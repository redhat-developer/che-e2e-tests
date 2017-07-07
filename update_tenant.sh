#!/bin/bash

server_runs() {
  server_log=$(oc -n ${OSO_NAMESPACE} logs dc/che 2> /dev/null)
  if [[ $(echo "${server_log}" | grep -E 'Server startup in [0-9]*' | wc -l) -ne 1 ]]; then
    echo "false" 
  else
    echo "true"
  fi
}

# Wait up to 3 minute for running Che server in pod 
wait_for_che_server_deployment() {
  counter=0
  timeout=180
  echo "Waiting until Che server in Che pod is running."
  set +x
  while [[ $(server_runs) == "false" ]]; do
      counter=$((counter+1))
      if [ $counter -gt $timeout ]; then
	  set -x
          echo "Server log does not contain information about server startup. Timeouted after ${timeout} seconds."
          exit 1
      fi
      sleep 1
  done
  set -x
  echo "Che server is running."
}


yum install -y centos-release-openshift-origin epel-release
yum install -y origin-clients jq

source config

set +x
OSO_TOKEN=$(curl -X GET -H "Authorization: Bearer ${KEYCLOAK_TOKEN}" \
	https://sso.openshift.io/auth/realms/fabric8/broker/openshift-v3/token  | jq .access_token | cut -d\" -f2)
if [[ "null" == "${OSO_TOKEN}" ]]; then
    set -x	
    echo "OSO token is empty, cannot proceed with verification. Something went wrong when obtaining it via keycloak."
    exit 1
fi

set -x
OSIO_VERSION=$(curl -sSL http://central.maven.org/maven2/io/fabric8/online/apps/che/maven-metadata.xml | grep latest | sed -e 's,.*<latest>\([^<]*\)</latest>.*,\1,g')

echo "Logging in to OpenShift ${OSO_MASTER_URL}"
set +x
oc login ${OSO_MASTER_URL} --token=${OSO_TOKEN}
set -x
oc project ${OSO_NAMESPACE}
CURRENT_DC_REVISION=$(oc get dc/che -o=custom-columns=NAME:.status.latestVersion --no-headers)
NEXT_DC_REVISION=$((CURRENT_DC_REVISION+1))
DOCKER_HUB_NAMESPACE_SANITIZED=${DOCKER_HUB_NAMESPACE/\//\\\/}
current_tag=$(oc get dc/che -o yaml | grep 'image:' | cut -d: -f 3)
if [[ "${current_tag}" != "${CHE_SERVER_DOCKER_IMAGE_TAG}" ]]; then
  echo "Updating Che server"
  echo "Getting version of OSIO and applying template"
  curl -sSL http://central.maven.org/maven2/io/fabric8/online/apps/che/${OSIO_VERSION}/che-${OSIO_VERSION}-openshift.yml | \
      sed "s/    hostname-http:.*/    hostname-http: ${OSO_HOSTNAME}/" | \
      sed "s/          image:.*/          image: ${DOCKER_HUB_NAMESPACE_SANITIZED}:${CHE_SERVER_DOCKER_IMAGE_TAG}/" | \
  oc apply --force=true -f -
  sleep 10

  ## Check status of deployment
  che_server_status=$(oc get pods | grep che-${NEXT_DC_REVISION} | grep -v che-${NEXT_DC_REVISION}-deploy | awk '{print $3}')
  counter=0
  timeout=360
  echo "Checking state of Che server pod for ${timeout} seconds"
  # Wait up to 6 minutes for running Che pod
  set +x
  while [ "${che_server_status}" != "Running" ]; do
      che_server_status=$(oc get pods | grep che-${NEXT_DC_REVISION} | grep -v che-${NEXT_DC_REVISION}-deploy | awk '{print $3}')
      counter=$((counter+1))
      if [ $counter -gt $timeout ]; then
	  set -x
          echo "Che server is not running after ${timeout} seconds"
          exit 1
      fi
      sleep 1
  done
  set -x
  echo "Che pod is running."
  
  wait_for_che_server_deployment
else
  echo "Che server already has specified tag, skipping Che server update and checking whether Che server runs."
  current_replicas=$(oc get dc/che | awk 'NR>1 { print $3 }')
  if [[ "${current_replicas}" == "0" ]]; then
    echo "Che server is not running, scaling Che server to 1 pod."
    oc scale dc/che --replicas=1
    wait_for_che_server_deployment
  fi
fi
