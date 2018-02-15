#!/bin/bash
set -x
set -e

# We need to disable selinux for now
/usr/sbin/setenforce 0

# Get all the deps in
yum -y install \
  docker \
  make \
  git

curl -vsLO https://github.com/stedolan/jq/releases/download/jq-1.5/jq-linux64
mv jq-linux64 /usr/bin/jq
chmod +x /usr/bin/jq
cp /usr/bin/jq ./jq

service docker start

function rebaseIfPR(){
  # Fetch PR and rebase on master, if job runs from PR
  cat jenkins-env \
      | grep -E "(ghprbSourceBranch|ghprbPullId)=" \
      | sed 's/^/export /g' \
      > /tmp/jenkins-env
  source /tmp/jenkins-env
  if [[ ! -z "${ghprbPullId:-}" ]] && [[ ! -z "${ghprbSourceBranch:-}" ]]; then
    echo 'Checking out to Github PR branch'
    git fetch origin pull/${ghprbPullId}/head:${ghprbSourceBranch}
    git checkout ${ghprbSourceBranch}
    git fetch origin master
    git rebase FETCH_HEAD
  else
    echo 'Working on current branch of EE tests repo'
  fi
}

if [ "$DO_NOT_REBASE" = "true" ]; then
  echo "Rebasing denied by variable DO_NOT_REBASE"
else
  rebaseIfPR
fi

# Set credentials
set +x

#prepend "export " and remove space after "="
cat jenkins-env \
    | grep -E "(CUSTOM_CHE|OSIO|KEYCLOAK|BUILD_NUMBER|JOB_NAME)" \
    | sed 's/^/export /g' \
    | sed 's/= /=/g' \
    > export_env_variables

source export_env_variables

CURL_OUTPUT=$(curl -sH "Content-Type: application/json" -X POST -d '{"refresh_token":"'$KEYCLOAK_TOKEN'"}' https://auth.${OSIO_URL_PART}/api/token/refresh)
export ACTIVE_TOKEN=$(echo $CURL_OUTPUT | jq --raw-output ".token | .access_token")
if [[ -z "${OSIO_USERNAME}" ]]; then
  empty_credentials="OSIO username is empty, "
fi
if [[ -z "${OSIO_PASSWORD}" ]]; then
  empty_credentials=${empty_credentials}"OSIO password is empty, "
fi
if [[ -z "${ACTIVE_TOKEN}" ]]; then
  empty_credentials=${empty_credentials}"Keycloak token is empty"
fi
if [[ ! -z "${empty_credentials}" ]]; then
  echo ${empty_credentials}
  exit 1
else
  echo 'OpenShift username and password and Keycloak token are not empty.'
fi

#echo username (after 5 characters there is added space to be able to echo it)
size=${#OSIO_USERNAME}
first_part=${OSIO_USERNAME:0:5}
last_part=${OSIO_USERNAME:5:$size}

PARSED_NAME=$first_part" "$last_part
echo "Tests are executed with user: $PARSED_NAME"

#set MASTER_URL
source ./cico/set_master_url.sh "${ACTIVE_TOKEN}"

./cico/validate_jwt_token.sh "${ACTIVE_TOKEN}"
if [ ! ./cico/validate_jwt_token.sh ]; then
  echo "Keycloak token is expired!"
  exit 1
else
  echo "Keycloak token is valid."
fi

if [[ $(curl -sX GET -H "Authorization: Bearer ${ACTIVE_TOKEN}" https://auth.${OSIO_URL_PART}/api/token?for=${OSO_MASTER_URL} \
   |  grep access_token | wc -l) -ne 1 ]]; then
  echo "Auth service returned error."
  exit 1
else
  echo "Keycloak token is alive. Proceeding with EE tests."
fi

echo 'export OSIO_USERNAME='${OSIO_USERNAME} >> ./env-vars
echo 'export OSIO_PASSWORD='${OSIO_PASSWORD} >> ./env-vars
echo 'export CUSTOM_CHE_SERVER_FULL_URL='${CUSTOM_CHE_SERVER_FULL_URL} >> ./env-vars
echo 'export KEYCLOAK_TOKEN='${ACTIVE_TOKEN} >> ./env-vars
echo 'export OSO_MASTER_URL='${OSO_MASTER_URL} >> ./env-vars
set -x
