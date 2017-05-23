#!/bin/bash

# Show command before executing
set -x

set -e

# Source environment variables of the jenkins slave
# that might interest this worker.
if [ -e "jenkins-env" ]; then
  cat jenkins-env \
    | grep -E "(JENKINS_URL|GIT_BRANCH|GIT_COMMIT|BUILD_NUMBER|ghprbSourceBranch|ghprbActualCommit|BUILD_URL|ghprbPullId|OSIO|KEYCLOAK)=" \
    | sed 's/^/export /g' \
    > /tmp/jenkins-env
  source /tmp/jenkins-env
fi

# We need to disable selinux for now
/usr/sbin/setenforce 0

# Get all the deps in
yum -y install \
  docker \
  make \
  git
service docker start

# Set scripts executable
chmod +x run_EE_tests.sh
chmod +x docker-entrypoint.sh

# Build EE test image
cp /tmp/jenkins-env .
docker build -t che-selenium .
mkdir -p dist && docker run --detach=true --name=che-selenium --user=fabric8 -e KEYCLOAK_TOKEN=$KEYCLOAK_TOKEN -e OSIO_USERNAME=$OSIO_USERNAME -e OSIO_PASSWORD=$OSIO_PASSWORD -t -v $(pwd)/dist:/dist:Z che-selenium

## Exec EE tests
docker exec che-selenium ./run_EE_tests.sh

## cat the test log to stdout
docker exec che-selenium cat ./functional_tests.log

## Test results to archive
docker cp che-selenium:/home/fabric8/che/tests/target/ .
docker cp che-selenium:/home/fabric8/che/functional_tests.log target


