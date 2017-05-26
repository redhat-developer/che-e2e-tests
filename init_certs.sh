#!/bin/bash

export OSO_ADDRESS=api.starter-us-east-2.openshift.com
export OSO_DOMAIN_NAME=api.starter-us-east-2.openshift.com
export KUBERNETES_CERTS_CA_FILE="/home/fabric8/che/api.starter-us-east-2.openshift.com.cer"

echo "Downloading and compiling certs installation"
git clone https://github.com/almighty/InstallCert.git
cd InstallCert
javac InstallCert.java

echo "Import the remote certificate from ${OSO_ADDRESS}"
java InstallCert $OSO_ADDRESS << ANSWERS
1
ANSWERS

echo "Export the certificate into the keystore for ${OSO_DOMAIN_NAME}"
keytool -exportcert -alias $OSO_DOMAIN_NAME-1 -keystore jssecacerts -storepass changeit -file $KUBERNETES_CERTS_CA_FILE
