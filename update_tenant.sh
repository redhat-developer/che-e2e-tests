#!/bin/bash

yum install -y centos-release-openshift-origin
yum install -y origin-clients

source config

set +x
OSO_TOKEN=$(curl -X GET -H "Authorization: Bearer ${KEYCLOAK_TOKEN}" \
	https://sso.openshift.io/auth/realms/fabric8/broker/openshift-v3/token  | jq .access_token | cut -d\" -f2)
if [[ "null" == "${OSO_TOKEN}" ]]; then
    echo "OSO token is empty, cannot proceed with verification. Something went wrong when obtaining it via keycloak."
    exit 1
fi

OSIO_VERSION=$(curl -sSL http://central.maven.org/maven2/io/fabric8/online/apps/che/maven-metadata.xml | grep latest | sed -e 's,.*<latest>\([^<]*\)</latest>.*,\1,g')

oc login ${OSO_MASTER_URL} --token=${OSO_TOKEN}
oc project ${OSO_NAMESPACE}
echo "Getting version of OSIO and applying template"
curl -sSL http://central.maven.org/maven2/io/fabric8/online/apps/che/${OSIO_VERSION}/che-${OSIO_VERSION}-openshift.yml | \
    sed "s/    hostname-http:.*/    hostname-http: ${OSO_HOSTNAME}/" | \
    sed "s/          image:.*/          image: rhche\/che-server:${CHE_SERVER_DOCKER_IMAGE_TAG}/" | \
oc apply -f -
set -x
## Check status of deployment
che_server_status=$(oc get pods | awk '{ if ($1 ~ /che-[0-9]+-.*/ && $1 !~ /che-[0-9]+-deploy/) print $3 }')
counter=0
timeout=240
echo "Checking state of Che pod."
# Wait up to 1 minutes for running Che pod
while [ "${che_server_status}" != "Running" ]; do
    che_server_status=$(oc get pods | awk '{ if ($1 ~ /che-[0-9]+-.*/ && $1 !~ /che-[0-9]+-deploy/) print $3 }')
    counter=$((counter+1))
    if [ $counter -gt $timeout ]; then
        echo "Che server is not running after ${timeout} seconds"
        exit 1
    fi
    sleep 1
done
echo "Che pod is running."
server_log=$(oc logs dc/che)
counter=0
timeout=180
echo "Checking whether a Che server in Che pod has already started."
# Wait up to 1 minute for running Che server in pod 
while [[ $(echo "${server_log}" | grep "Server startup in" | wc -l) -ne 1 ]]; do
    server_log=$(oc logs dc/che)
    counter=$((counter+1))
    if [ $counter -gt $timeout ]; then
        echo "Server log does not contain information about server startup. Timeouted after ${timeout} seconds."
        exit 1
    fi
    sleep 1
done
echo "Che server is running."
