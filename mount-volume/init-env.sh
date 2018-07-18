#!/usr/bin/env bash
#Install oc
wget https://github.com/openshift/origin/releases/download/v3.7.2/openshift-origin-client-tools-v3.7.2-282e43f-linux-64bit.tar.gz

tar -xvf openshift-origin-client-tools-v3.7.2-282e43f-linux-64bit.tar.gz
mv openshift-origin-client-tools-v3.7.2-282e43f-linux-64bit/oc $(pwd)/oc
rm -rf openshift-origin-client-tools-v3.7.2-282e43f-linux-64bit

# Install jq
wget https://github.com/stedolan/jq/releases/download/jq-1.5/jq-linux64

mv jq-linux64 $(pwd)/jq
chmod +x $(pwd)/jq
