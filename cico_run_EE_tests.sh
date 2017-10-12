#!/bin/bash

archive_artifacts(){
	echo "Archiving artifacts"
	DATE=$(date +%Y%m%d-%H%M%S)
	echo "With date $DATE"
	ls -la ./artifacts.key
	chmod 600 ./artifacts.key
	rsync --password-file=./artifacts.key -PHva  /home/fabric8/che/tests/target/* devtools@artifacts.ci.centos.org::devtools/che-functional-tests/$DATE
}

set -x
set -e
set +o nounset

# Load configuration
if [[ $# -gt 0 ]]; then
  if [[ ! -f $1 ]]; then
    echo "Provided argument is not a valid config file. Default config will be used."
  else
    echo "Replacing default config by the provided one."
    mv $1 config
  fi
fi
echo "Sourcing configuration."
source config

# Prepare environment - git repo, credentials, token validation
source prepare_environment.sh

# Update tenant
source update_tenant.sh

# Run test image
cat /tmp/jenkins-env >> ./env-vars
chown -R 1000:1000 ./*
docker run -d --user=fabric8 --cap-add SYS_ADMIN --name=che-selenium -t -v $(pwd):/home/fabric8/che:Z mlabuda/che-selenium:170621

## Exec tests
docker exec --user=fabric8 che-selenium /home/fabric8/che/run_EE_tests.sh || RETURN_CODE=$? && true
archive_artifacts

exit $RETURN_CODE