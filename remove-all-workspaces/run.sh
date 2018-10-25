#!/usr/bin/env bash
tokens_file="tokens.txt"
#cat $USERS_PROPERTIES_FILE > users.properties

# get tokens for users defined in users.properties
echo "GETTING TOKENS"
./get_active_tokens.sh $tokens_file

# removing workspaces
echo "REMOVING WORKSPACES"
./remove_workspaces.sh $tokens_file

echo "******** REPORT: ********"
cat ./report.txt
#rm -f loginfile.html tokens.txt
