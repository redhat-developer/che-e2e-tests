chmod +x simple-pod.sh
chmod +x init-env.sh
set +e

USERNAME=$1
PASSWORD=$2
URL=$3

./init-env.sh
./simple-pod.sh $USERNAME $PASSWORD $URL

RESULT=$?
set -e
oc get events

#try delete pod just for sure
oc get pod simple-pod -o json || true
oc delete pod simple-pod || true

exit $RESULT