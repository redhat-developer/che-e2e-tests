#!/usr/bin/env bash

LOGFILE=$(pwd)/functional_tests.log
echo Using logfile $LOGFILE

# Run tests
echo Running tests...
set +x
. ~/che/config
cd ~/che
scl enable rh-maven33 'mvn clean verify -DopenShiftMasterURL=$OSO_MASTER_URL -DkeycloakToken=$KEYCLOAK_TOKEN -DopenShiftNamespace=$OSO_NAMESPACE -DosioUsername=$OSIO_USERNAME -DosioPassword=$OSIO_PASSWORD'
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


