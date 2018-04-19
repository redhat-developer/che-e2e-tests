while IFS= read -r tokens
do
  token=$(echo $tokens| cut -d';' -f 1)

  response=$(curl -H "Authorization: Bearer $token" $CHE_SERVER_URL/api/workspace/)
  ids=$(echo $response  | jq --raw-output '.[] | .id')
  statuses=$(echo $response  | jq --raw-output '.[] | .status')
  idarr=($ids)
  statusarr=($statuses)
  counter=0

  for i in "${!idarr[@]}"; do
    if [[ "${statusarr[$i]}" = "STOPPED" ]]; then
        echo "Deleting workspace: ${idarr[$i]}"
        curl -H "Authorization: Bearer $token" -X DELETE $CHE_SERVER_URL/api/workspace/${idarr[$i]}
    else
        curl -H "Authorization: Bearer $token" -X DELETE $CHE_SERVER_URL/api/workspace/${idarr[$i]}/runtime
        echo "Waiting for workspace to stop"
        while true;
        do
          resp=$(curl -H "Authorization: Bearer $token" -X GET $CHE_SERVER_URL/api/workspace/${idarr[$i]})
          status=$(echo $resp | jq --raw-output '.status')
          if [ $status = "STOPPED" ]; then
            break
          else
            echo "Status is $status, waiting for STOPPED"
          fi
        done
        echo "Deleting workspace: ${idarr[$i]}"
        curl -H "Authorization: Bearer $token" -X DELETE $CHE_SERVER_URL/api/workspace/${idarr[$i]}
    fi
  done
done < "$TOKENS_FILE"
