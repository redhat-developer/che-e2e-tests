#!/usr/bin/env bash
# First parameter either "Start" or "Stop"
function podHasStatus {
    #pod status is not accessible - pod was stopped and deleted
    SIMPLE_POD_JSON=$(oc get pod simple-pod -o json)
    RETURN_CODE=$?
    POD_STATUS=$(echo $SIMPLE_POD_JSON | jq --raw-output '.status.phase')
    echo "Wanted: $1      Actual: $POD_STATUS"
    if [[ $POD_STATUS == "Running" ]]; then
        if [[ $1 == "Start" ]]; then
            return 0
        else
            return 1
        fi
    elif [[ $POD_STATUS == "" ]]; then
        if [[ $1 == "Stop" ]]; then
            return 0
        fi
        if [ $RETURN_CODE -eq 0 ]; then
            return 1
        else
            return 0
        fi
    else
        return 1
    fi
}

function waitForPod {
    TIMEOUT=$1
    START_STOP=$2
    CURRENT_TRY=1
    if [[ $2 == "Start" ]]; then
        echo "Waiting for pod to start"
    elif [[ $2 == "Stop" ]]; then
        echo "Waiting for pod to stop"
    fi

    start=$(($(date +%s%N)/1000000))
    while [[ $CURRENT_TRY -le $TIMEOUT ]]; do
        echo "Try #$CURRENT_TRY"
        if podHasStatus $START_STOP; then
            echo "Pod has desired status"
            end=$(($(date +%s%N)/1000000))
            echo `expr $end - $start` >> $START_STOP.csv
            return
        else
            CURRENT_TRY=$(($CURRENT_TRY+1))
            sleep 1
            continue
        fi
    done
    echo "Waiting for pod to be running timed out. Exiting."
    exit 1
}

function waitForPodToBeRunning {
    waitForPod 120 "Start"
}

function waitForPodToStop {
    waitForPod 60 "Stop"
}

COUNTER=1
USERNAME=$1
PASSWORD=$2
URL=$3
VOLUME_NAME=$4

chrlen=$((${#USERNAME}-3))
echo "running tests with user: ${USERNAME:0:3} ${USERNAME:3:$chrlen}"
oc login $URL -u $USERNAME -p $PASSWORD
oc project $USERNAME
echo "max tries: $MAX_TRIES"

SIMPLE_POD_CONFIGURATION_JSON=$(jq ".spec.volumes[].persistentVolumeClaim.claimName |= \"$VOLUME_NAME\"" simple-pod.json)

while [[ $COUNTER -le $MAX_TRIES ]]; do
    echo "ITERATION #$COUNTER"
    echo "$SIMPLE_POD_CONFIGURATION_JSON" | oc apply -f -
    echo "$SIMPLE_POD_CONFIGURATION_JSON" | oc apply -f -
    waitForPodToBeRunning

    oc delete pod simple-pod
    waitForPodToStop

    sleep 10

    echo "Increasing iteration counter"
    COUNTER=$(($COUNTER+1))
done

echo "Script finished"
