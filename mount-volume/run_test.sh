chmod +x simple-pod.sh
chmod +x init-env.sh
set +e

USERNAME=$1
PASSWORD=$2
URL=$3

echo "Initializing environment"
./init-env.sh

PATH=$PATH:$(pwd)/openshift-origin-client-tools-v3.7.2-282e43f-linux-64bit

echo "Running tests"
./simple-pod.sh $USERNAME $PASSWORD $URL
./zabbix.sh $URL

RESULT=$?
set -e
#oc get events

#try delete pod just for sure
oc get pod simple-pod -o json || true
oc delete pod simple-pod || true

exit $RESULT