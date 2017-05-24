#!/bin/bash

# Show command before executing
set -x

set -e

cat jenkins-env \
    | grep -E "(JENKINS_URL|GIT_BRANCH|GIT_COMMIT|BUILD_NUMBER|ghprbSourceBranch|ghprbActualCommit|BUILD_URL|ghprbPullId)=" \
    | sed 's/^/export /g' \
    > /tmp/jenkins-env
source /tmp/jenkins-env

# We need to disable selinux for now
/usr/sbin/setenforce 0

# Get all the deps in
yum -y install \
  docker \
  make \
  git
service docker start

# Fetch PR and rebase on master
git fetch origin pull/${ghprbPullId}/head:${ghprbSourceBranch}
git checkout ${ghprbSourceBranch}
git fetch origin master
git rebase FETCH_HEAD

# Set credentials
set +x
cat jenkins-env \
    | grep -E "(OSIO|KEYCLOAK)=" \
    | sed 's/^/export /g' \
    > credential_file
source credential_file
if [[ -z "${OSIO_USERNAME}" ]]; then
  empty_credentials="OSIO username is empty, "
fi 
if [[ -z "${OSIO_PASSWORD}" ]]; then
  empty_credentials=${empty_credentials}"OSIO password is empty, "
fi
if [[ -z "${KEYCLOAK_TOKEN}" ]]; then
  empty_credentials=${empty_credentials}"Keycloak token is empty"
fi
if [[ ! -z "${empty_credentials}" ]]; then
  echo ${empty_credentials}
  exit 1
else
  echo 'OpenShift username and password and Keycloak token are not empty. Proceeding with EE tests.'
fi
echo 'export OSIO_USERNAME='${OSIO_USERNAME} >> ./env-vars
echo 'export OSIO_PASSWORD='${OSIO_PASSWORD} >> ./env-vars
echo 'export KEYCLOAK_TOKEN='${KEYCLOAK_TOKEN} >> ./env-vars
set -x

# Set scripts executable
chmod +x run_EE_tests.sh
chmod +x docker-entrypoint.sh

# Build EE test image
cat /tmp/jenkins-env >> ./env-vars
docker build -t che-selenium .
mkdir -p dist && docker run --detach=true --name=che-selenium --user=fabric8 -t -v $(pwd)/dist:/dist:Z che-selenium

## Exec EE tests
docker exec che-selenium ./run_EE_tests.sh

## cat the test log to stdout
docker exec che-selenium cat ./functional_tests.log

## Test results to archive
docker cp che-selenium:/home/fabric8/che/tests/target/ .
docker cp che-selenium:/home/fabric8/che/functional_tests.log target


