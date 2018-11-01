#!/usr/bin/env bash
# First parameter either "Start" or "Stop"
function podHasStatus {
	ACTION=$1
	POD_NAME=$2
	
    SIMPLE_POD_JSON=$(oc get pod "$POD_NAME" -o json)
    RETURN_CODE=$?
    
    if [[ $RETURN_CODE -ne 0 ]]; then
    	if [[ $ACTION == "Stop" ]]; then
    		echo "Pod was stopped and removed."
    		return 0
		else 
			echo "Could not obtain information about pod. Trying again."
			return 1
		fi
	fi
    
    POD_STATUS=$(echo "$SIMPLE_POD_JSON" | jq --raw-output '.status.phase')
    if [[ $ACTION == "Start" ]]; then
    	if [[ $POD_STATUS == "Running" ]]; then
    		echo "Wanted: Running     Actual: ${POD_STATUS}"
    		echo "Pod is running."
    		return 0
	    else 
	    	echo "Wanted: Running     Actual: ${POD_STATUS}"
	    	return 1
    	fi
	else
		echo "Waiting for pod removal. Actual status: ${POD_STATUS}"
		return 1
	fi
}

function waitForPod {
    START_STOP=$1
    POD_NAME=$2
    CURRENT_TRY=1
    if [[ $START_STOP == "Start" ]]; then
        echo "Waiting for pod to start"
    elif [[ $START_STOP == "Stop" ]]; then
        echo "Waiting for pod to be removed"
    fi

    start=$(($(date +%s%N)/1000000))
    while [[ ${CURRENT_TRY} -le ${ATTEMPT_TIMEOUT} ]]; do
        if podHasStatus "$START_STOP" "$POD_NAME"; then
            end=$(($(date +%s%N)/1000000))
            expr $end - $start >> "$START_STOP".csv
            return
        else
            CURRENT_TRY=$((CURRENT_TRY+1))
            sleep 1
            continue
        fi
    done
    echo "Waiting for pod to be running timed out. Exiting."
    exit 1
}

function waitForPodToBeRunning {
    waitForPod "Start" "simple-pod"
}

function waitForPodToStop {
    waitForPod "Stop" "simple-pod"
}

function simplePodRunTest {
	COUNTER=1
	echo "Number of iterations: ${ITERATIONS}"
	
	SIMPLE_POD_CONFIGURATION_JSON=$(jq ".spec.volumes[].persistentVolumeClaim.claimName |= \"$VOLUME_NAME\"" simple-pod.json)
	
	while [[ ${COUNTER} -le ${ITERATIONS} ]]; do
	    echo "ITERATION #${COUNTER}"
	    echo "$SIMPLE_POD_CONFIGURATION_JSON" | oc apply -f -
	    echo "$SIMPLE_POD_CONFIGURATION_JSON" | oc apply -f -
	    waitForPodToBeRunning
	
	    oc delete pod simple-pod
	    waitForPodToStop
	
	    COUNTER=$((COUNTER+1))
	done
	
	echo "Tests were done."
}
	