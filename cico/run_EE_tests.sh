#!/usr/bin/env bash

# Run tests
echo Running tests...
set +x
cd /home/fabric8/che
source $1
source env-vars

/home/fabric8/che/cico/validate_jwt_token.sh "${KEYCLOAK_TOKEN}"

if [[ -z "${OSO_MASTER_URL}" ]]; then
  echo "OSO Master URL env var is empty"
  exit 1
fi
if [[ -z "${OSO_NAMESPACE}" ]]; then
  echo "OSO namespac env var is empty"
  exit 1
fi
if [[ -z "${OSIO_USERNAME}" ]] || [[ -z "${OSIO_PASSWORD}" ]]; then
  echo "One or more credentials is not set, cannot proceed with tests"
  exit 1
fi

#check token
TOKEN=$KEYCLOAK_TOKEN
TOKEN_PARTS_INDEX=0
IFS='.' read -ra TOKEN_PARTS <<< "${TOKEN}"
for i in "${TOKEN_PARTS[@]}"; do
    TOKEN_PARTS_INDEX=$((TOKEN_PARTS_INDEX + 1))
done
echo "Active token has $TOKEN_PARTS_INDEX sections."
if [[ ! TOKEN_PARTS_INDEX -eq 3 ]]; then
  echo "JWT token parse failed!"
  exit 1
fi
echo "MD5:$(echo -n "${TOKEN}" | md5sum | awk '{print $1}')"
TOKEN_EXP=$(echo "${TOKEN_PARTS[1]}" | base64 -d - 2>/dev/null | ./jq .exp)
EMAIL=$(echo "${TOKEN_PARTS[1]}" | base64 -d - 2>/dev/null | ./jq .email)
CURRENT_UNIX_TIME=$(date +%s)
echo "Active token exp:${TOKEN_EXP}"
echo "Active token exp:${EMAIL}"
echo "Current time in mils:${CURRENT_UNIX_TIME}"

#end check token

cd /home/fabric8/che
export DISPLAY=:99
mvn clean install -DskipTests
if [[ -z "${CUSTOM_CHE_SERVER_FULL_URL}" ]]; then
  mvn clean verify -f tests/pom.xml -DosioUrlPart=$OSIO_URL_PART -Dtest=$TEST_SUITE -DopenShiftMasterURL=$OSO_MASTER_URL -DkeycloakToken=$KEYCLOAK_TOKEN -DopenShiftNamespace=$OSO_NAMESPACE -DosioUsername=$OSIO_USERNAME -DosioPassword=$OSIO_PASSWORD
else
  mvn clean verify -f tests/pom.xml -DosioUrlPart=$OSIO_URL_PART -Dtest=$TEST_SUITE -DkeycloakToken=$KEYCLOAK_TOKEN -DosioUsername=$OSIO_USERNAME -DosioPassword=$OSIO_PASSWORD -DcustomCheServerFullURL=$CUSTOM_CHE_SERVER_FULL_URL
fi
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


