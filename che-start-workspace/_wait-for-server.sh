#!/bin/bash

source _setenv.sh

NOW=`date +%s`
STOP=`expr $NOW + 60`

while [ `date +%s` -lt $STOP ];
do
   echo "Checking if the Core server is up and running ..."
   curl --silent $SERVER_SCHEME://$SERVER_HOST:$AUTH_PORT/api/status
   if [[ $? -eq 0 ]]; then
     response_code=`curl -i --silent $SERVER_SCHEME://$SERVER_HOST:$AUTH_PORT/api/status | head -n 1 | cut -d " " -f2`;
     if [[ "$response_code" -eq "200" ]]; then
       break;
     else
       echo "The Core server is not ready - responding by $response_code code.";
     fi;
   else
     echo "The Core server is not responding.";
   fi
   echo "Trying again after 10s.";
   sleep 10;
done