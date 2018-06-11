chmod +x simple-pod.sh
chmod +x init-env.sh
set +e

echo $1
echo $2

./init-env.sh
./simple-pod.sh $1 $2

RESULT=$?
set -e
oc get events

#get logs
wget --no-check-certificate -O console-output.log ${BUILD_URL}/consoleText

#try delete pod just for sure
oc get pod simple-pod -o json || true
oc delete pod simple-pod || true

exit $RESULT