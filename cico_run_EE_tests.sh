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

# Build tests image and run it
cat /tmp/jenkins-env >> ./env-vars
docker build -t che-selenium .
mkdir -p dist && docker run --detach=true --user=root --cap-add SYS_ADMIN --name=che-selenium -t -v $(pwd)/dist:/dist:Z che-selenium

## Exec tests
docker exec --user=fabric8 che-selenium ./run_EE_tests.sh

