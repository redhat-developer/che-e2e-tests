#!/usr/bin/env bash

RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

tokens_file=$1
IFS=$'\n'       # make newlines the only separator
for j in $(cat ./users.properties)    
do
    username=$(echo "$j" | cut -d";" -f 1)
    password=$(echo "$j" | cut -d";" -f 2)
    env=$(echo "$j" | cut -d";" -f 3)
    echo "Trying to find token for $username"  
    
    #verify environment - if production or prod-preview
    #variable preview is used differ between prod and prod-preview urls
	if [ "$env" == "prod" ]; then
    	response=$(curl -s --header 'Accept: application/json' -X GET "https://api.openshift.io/api/users?filter[username]=$username")
		data=$(echo $response | jq .data)
		if [ "$data" == "[]" ]; then
			echo "${RED}User $username is not provisoned on $env cluster. Please check settings. Skipping user.${NC}"
			continue
	    fi
	    preview=""
	else
		response=$(curl -s --header 'Accept: application/json' -X GET "https://api.prod-preview.openshift.io/api/users?filter[username]=$username")
		data=$(echo $response | jq .data)
		if [ "$data" == "[]" ]; then
			echo -e "${RED}User $username is not provisioned on $env cluster. Please check settings. Skipping user.${NC}" 
			continue
		fi
		preview="prod-preview."
    fi
		
	#get html of developers login page
	curl -sX GET -L -c cookie-file -b cookie-file "https://auth.${preview}openshift.io/api/login?redirect=https://che.openshift.io" > loginfile.html
	
	#get url for login from form
	url=$(grep "form id" loginfile.html | grep -o 'http.*.tab_id=.[^\"]*')
	dataUrl="username=$username&password=$password&login=Log+in"
	url=$(echo $url | sed 's/\&amp;/\&/g')
	
	#send login and follow redirects  
    url=$(curl -w '%{redirect_url}' -s -X POST -c cookie-file -b cookie-file -d $dataUrl $url)
	found=$(echo $url | grep "token_json")
	
	while [ true ]
	do
		url=$(curl -c cookie-file -b cookie-file -s -o /dev/null -w '%{redirect_url}' $url)
		if [[ ${#url} == 0 ]]; then
			#all redirects were done but token was not found
			break
		fi
		found=$(echo $url | grep "token_json")
		if [[ ${#found} > 0 ]]; then
			#some redirects were done and token was found as a part of url
			break
		fi
	done

	#substract active token
	token=$(echo $url | grep -o ey.[^\%]* | head -1)
	if [[ ${#token} > 0 ]]; then
	#save each token into file tokens.txt in format: token;username;["","prod-preview"]
		echo "$token;$username;$preview" >> tokens_file
		echo -e "${GREEN}Token for user $username was found successfully.${NC}"
	else
		echo -e "${RED}Failed to obtain token for $username! Probably user password is incorrect. Continue with other users. ${NC}"
	fi
	token=""
	rm cookie-file
done
