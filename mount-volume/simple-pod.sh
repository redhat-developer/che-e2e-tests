#!/bin/bash
# First parameter either "Start" or "Stop"
function podHasStatus {
    SIMPLE_POD_JSON=$(oc get pod simple-pod -o json)
    RETURN_CODE=$?
    POD_STATUS=$(echo $SIMPLE_POD_JSON | jq --raw-output '.status.phase')
    echo "Pod status: $POD_STATUS, return code: $RETURN_CODE"
    if [[ $POD_STATUS == "Running" ]]; then
        if [[ $1 == "Start" ]]; then
            return 0
        else
            return 1
        fi
    else
        if [[ $1 == "Stop" ]]; then
            if [ $RETURN_CODE -eq 0 ]; then
                return 1
            else
                return 0
            fi
        else
            return 1
        fi
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
    while [[ $CURRENT_TRY -le $TIMEOUT ]]; do
        echo "Waiting for pod to have desired status:$START_STOP. Try #$CURRENT_TRY"
        if podHasStatus $START_STOP; then
            echo "Pod has desired status"
            return
        else
            echo "Pod does not have desided status"
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
MAX_TRIES=5

oc login https://api.starter-us-east-2.openshift.com:443 -u $1 -p $2
oc project $1

while [[ $COUNTER -le $MAX_TRIES ]]; do
    echo "ITERATION #$COUNTER"
    oc apply -f simple-pod.json
    waitForPodToBeRunning

    oc delete pod simple-pod
    waitForPodToStop

    sleep 10

    echo "Increasing iteration counter"
    COUNTER=$(($COUNTER+1))
done

echo "Script finished"