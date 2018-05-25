if [[ "$OSIO_USERNAME" == *"@"* ]]; then
    CURL=$(curl -sH "Content-Type: application/json" -X GET "https://auth.${OSIO_URL_PART}/api/userinfo" -H "Authorization: Bearer $1")
    export OSIO_USERNAME=$(echo $CURL | jq --raw-output ".preferred_username")
fi
CURL_OUTPUT=$(curl -sH "Content-Type: application/json" -X GET "https://auth.${OSIO_URL_PART}/api/users?filter\[username\]=${OSIO_USERNAME}")
OSO_MASTER_URL=$(echo $CURL_OUTPUT | jq --raw-output ".data[0].attributes.cluster")
OSO_MASTER_URL=${OSO_MASTER_URL:0:-1}
echo "OSO_MASTER_URL found and set to: $OSO_MASTER_URL"
echo "export OSO_MASTER_URL=$OSO_MASTER_URL" >> ./env-vars
