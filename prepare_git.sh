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
service docker start

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
