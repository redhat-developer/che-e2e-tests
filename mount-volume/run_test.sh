#!/usr/bin/env bash
chmod +x simple-pod.sh
chmod +x init-env.sh
set +e

USERNAME=$1
PASSWORD=$2
URL=$3
VOLUME_NAME=$4
VOLUME_NAME=${VOLUME_NAME:-"claim-che-workspace"}
ZABBIX_PREFIX=$5
ATTEMPT_TIMEOUT=$6
ATTEMPT_TIMEOUT=${ATTEMPT_TIMEOUT:-120}

echo "Initializing environment"
./init-env.sh

PATH=$PATH:$(pwd)/

echo "Running tests"
./simple-pod.sh $USERNAME $PASSWORD $URL $VOLUME_NAME $ATTEMPT_TIMEOUT
if [ $? == 0 ]; then
  ./zabbix.sh $URL $ZABBIX_PREFIX
else
  echo "Tests have failed. Not reporting data."
fi

RESULT=$?
set -e
#oc get events

#try delete pod just for sure
oc get pod simple-pod -o json || true
oc delete pod simple-pod || true

exit $RESULT
