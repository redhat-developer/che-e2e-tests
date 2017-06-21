#!/bin/bash
set -x
set -e

# Prepare environment - git repo, credentials, token validation
source prepare_environment.sh

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


# Update tenant
source update_tenant.sh

# Run test image
cat /tmp/jenkins-env >> ./env-vars
docker run --detach=true --user=root --cap-add SYS_ADMIN --name=che-selenium -t -v $(pwd):/home/fabric8/che mlabuda/che-selenium:170621

## Exec tests
docker exec --user=fabric8 che-selenium ./run_EE_tests.sh

