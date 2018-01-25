#!/usr/bin/env bash

# Run tests
echo Running tests...
set +x
cd /home/fabric8/che
source $1
source env-vars

if [[ -z "${OSO_MASTER_URL}" ]]; then
  echo "OSO Master URL env var is empty"
  exit 1
fi
if [[ -z "${OSO_NAMESPACE}" ]]; then
  echo "OSO namespac env var is empty"
  exit 1
fi
if [[ -z "${OSIO_USERNAME}" ]] || [[ -z "${OSIO_PASSWORD}" ]] || [[ -z "${KEYCLOAK_TOKEN}" ]]; then
  echo "One or more credentials is not set, cannot proceed with tests"
  exit 1
fi

cd /home/fabric8/che
export DISPLAY=:99
mvn clean install -DskipTests
mvn clean verify -f tests/pom.xml -DosioUrlPart=$OSIO_URL_PART -Dtest=$TEST_SUITE -DopenShiftMasterURL=$OSO_MASTER_URL -DkeycloakToken=$KEYCLOAK_TOKEN -DopenShiftNamespace=$OSO_NAMESPACE -DosioUsername=$OSIO_USERNAME -DosioPassword=$OSIO_PASSWORD
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


