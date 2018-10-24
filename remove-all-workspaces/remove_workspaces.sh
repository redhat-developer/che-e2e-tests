#!/usr/bin/env bash
tokens=$1

echo "User Number_of_workspaces" > report.txt
echo "_______________________________________________________" >> report.txt

while IFS= read -r tokens
do
  token=$(echo $tokens| cut -d';' -f 1)
  username=$(echo $tokens| cut -d';' -f 2)
  preview=$(echo $tokens| cut -d';' -f 3)

  response=$(curl -s -H "Authorization: Bearer $token" https://che.${preview}openshift.io/api/workspace/)
  ids=$(echo $response  | jq --raw-output '.[] | .id')
  statuses=$(echo $response  | jq --raw-output '.[] | .status')
  idarr=($ids)
  echo "Removing workspaces for user $username. Number of workspaces: ${#idarr[@]}"
  echo "$username ${#idarr[@]}" >> report.txt
  statusarr=($statuses)
  counter=0
  for i in "${!idarr[@]}"; do
    if [[ "${statusarr[$i]}" = "STOPPED" ]]; then
        echo "Deleting workspace: ${idarr[$i]}"
        curl -H "Authorization: Bearer $token" --no-keepalive -X DELETE https://che.${preview}openshift.io/api/workspace/${idarr[$i]}
    else
        curl -H "Authorization: Bearer $token" -X DELETE https://che.${preview}openshift.io/api/workspace/${idarr[$i]}/runtime
        echo "Waiting for workspace to stop"
        while true;
        do
          resp=$(curl -s -H "Authorization: Bearer $token" -X GET https://che.${preview}openshift.io/api/workspace/${idarr[$i]})
          status=$(echo $resp | jq --raw-output '.status')
          if [ $status = "STOPPED" ]; then
            break
          else
            echo "Status is $status, waiting for STOPPED"
          fi
        done
        echo "Deleting workspace: ${idarr[$i]}"
        curl -H "Authorization: Bearer $token" -X DELETE https://che.${preview}openshift.io/api/workspace/${idarr[$i]}
    fi
  done
done < "$tokens"
