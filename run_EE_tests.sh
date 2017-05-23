#!/usr/bin/env bash

LOGFILE=$(pwd)/functional_tests.log
echo Using logfile $LOGFILE

# Run tests
echo Running tests...
set +x
source ~/che/config
source ~/che/env-vars
source ~/che/init_certs.sh

cd ~/che
mvn clean verify -DopenShiftMasterURL=$OSO_MASTER_URL -DkeycloakToken=$KEYCLOAK_TOKEN -DopenShiftNamespace=$OSO_NAMESPACE -DosioUsername=$OSIO_USERNAME -DosioPassword=$OSIO_PASSWORD -Dkubernetes.certs.ca.file=$KUBERNETES_CERTS_CA_FILE
TEST_RESULT=$?
set -x

# Return test result
if [ $TEST_RESULT -eq 0 ]; then
  echo 'Functional tests OK'
  exit 0
else
  echo 'Functional tests FAIL'
  exit 1
fi


