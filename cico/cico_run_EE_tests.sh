#!/bin/bash
archive_artifacts(){
  echo "With date $DATE"
  ls -la ./artifacts.key
  chmod 600 ./artifacts.key
  chown root ./artifacts.key
  mkdir -p ./che-functional-tests/${JOB_NAME}/${BUILD_NUMBER}/surefire-reports
  cp -R ./tests/target/surefire-reports/*.txt ./che-functional-tests/${JOB_NAME}/${BUILD_NUMBER}/surefire-reports
  cp -R ./tests/target/screenshots/ ./che-functional-tests/${JOB_NAME}/${BUILD_NUMBER}/
  rsync --password-file=./artifacts.key -PHva --relative ./che-functional-tests/${JOB_NAME}/${BUILD_NUMBER} devtools@artifacts.ci.centos.org::devtools/
 }

set -x
set -e
set +o nounset

# Load configuration
CONFIG_FILE=./cico/config
if [[ $# -gt 0 ]]; then
  if [[ ! -f $1 ]]; then
    echo "Provided argument is not a valid config file. Default config will be used."
  else
    echo "Replacing default config by the provided one."
	CONFIG_FILE=$1
  fi
fi
echo "Sourcing configuration."
source ${CONFIG_FILE}

# Prepare environment - git repo, credentials, token validation
source ./cico/prepare_environment.sh

# Run test image
cat /tmp/jenkins-env >> ./env-vars
chown -R 1000:1000 ./*
docker run -d --user=fabric8 --cap-add SYS_ADMIN --name=che-selenium -t -v $(pwd):/home/fabric8/che:Z kkanova/che-selenium:latest

## Exec tests
docker exec --user=fabric8 che-selenium /home/fabric8/che/cico/run_EE_tests.sh $CONFIG_FILE || RETURN_CODE=$? && true
echo "Tests ended, now executing archiving artifacts."
archive_artifacts

exit $RETURN_CODE
