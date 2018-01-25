#!/bin/bash
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
#    mv $1 config
	CONFIG_FILE=$1
  fi
fi
echo "Sourcing configuration."
source ${CONFIG_FILE}

# Prepare environment - git repo, credentials, token validation
source ./cico/prepare_environment.sh

# Update tenant
#source update_tenant.sh

# Run test image
cat /tmp/jenkins-env >> ./env-vars
chown -R 1000:1000 ./*
docker run -d --user=fabric8 --cap-add SYS_ADMIN --name=che-selenium -t -v $(pwd):/home/fabric8/che:Z kkanova/che-selenium:latest

## Exec tests
docker exec --user=fabric8 che-selenium /home/fabric8/che/cico/run_EE_tests.sh $CONFIG_FILE

