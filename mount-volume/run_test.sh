#!/usr/bin/env bash

source ./getopts_util.sh
while getopts "hu:p:r:v:z:t:i:f" opt; do
  case $opt in
	h) printHelp
       exit 0
       ;;
    u) export USERNAME=$OPTARG
      ;;
    p) export PASSWORD=$OPTARG
      ;;
    r) export URL=$OPTARG
      ;;
    v) export VOLUME_NAME=$OPTARG
      ;;
    z) export ZABBIX_PREFIX=$OPTARG
      ;;
    t) export ATTEMPT_TIMEOUT=$OPTARG
      ;;
    i) export ITERATIONS=$OPTARG
      ;;
    f) export FILL_PVC="true"
      ;;
    \?)
      echo "\"$opt\" is an invalid option!"
      exit 1
      ;;
    :)
      echo "Option \"$opt\" needs an argument."
      ;;
  esac
done
checkParams

chmod +x simple-pod.sh
chmod +x init-env.sh


echo "Initializing environment"
./init-env.sh

#login and choose right project
chrlen=$((${#USERNAME}-3))
echo "running tests with user: ${USERNAME:0:3} ${USERNAME:3:$chrlen}"
oc login $URL -u $USERNAME -p $PASSWORD
if [[ $? != 0 ]]; then 
	echo "Can not login - please check credentials."
	exit 1
fi

oc project "$USERNAME"

echo "Ensure if PVC exists with required setup"
if ./setup_pvc.sh; then 
	echo -e "Setup completed. Continuing with tests.\\n"
else
	exit 1
fi

PATH=$PATH:$(pwd)/

echo "   RUNNING TESTS"
source ./simple-pod.sh
simplePodRunTest 
if [ $? == 0 ]; then
  ./zabbix.sh "$URL" "$ZABBIX_PREFIX" "$ITERATIONS"
  echo "Tests status: SUCCESS"
else
  echo "Tests status: FAILED - not reporting data to zabbix."
fi

set -e
#oc get events

#try delete pod just for sure
oc get pod simple-pod -o json || true
oc delete pod simple-pod || true

exit $RESULT
