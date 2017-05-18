#!/usr/bin/env bash

LOGFILE=$(pwd)/functional_tests.log
echo Using logfile $LOGFILE

# Start che starter
set +x
git clone https://github.com/redhat-developer/che-starter
cd che-starter
mvn clean install -DskipTests
nohup java -jar target/che-starter-1.0-SNAPSHOT.jar &

# Run tests
echo Running tests...
cat ~/payload/jenkins-env | grep KEYCLOAK > ~/.ee_test_params
cat ~/che/config >> ~/.ee_test_params
. ~/.ee_test_params
cd ~/che
mvn clean verify -DopenShiftMasterURL=$OSO_MASTER_URL -DkeycloakToken=$KEYCLOAK_TOKEN -DopenShiftNamespace=$OSO_NAMESPACE -DcheStarterURL=http://localhost:10000
TEST_RESULT=$?

# Kill che starter
CHE_STARTER_PID=$(jps | grep che-starter-1.0-SNAPSHOT.jar | cut -d" " -f1)
if [[ ! -z "${CHE_STARTER_PID}" ]]; then
  kill -9 ${CHE_STARTER_PID}
fi;
set -x

# Return test result
if [ $TEST_RESULT -eq 0 ]; then
  echo 'Functional tests OK'
  exit 0
else
  echo 'Functional tests FAIL'
  exit 1
fi


