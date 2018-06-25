# Tests for che running on openshift.io

In this repo you can find performance and functoinal tests. To describe each of them more precisly, there is README file in each folder to specify how
to run them etc.

## che-start-workspace tests

This test servers as monitoring for workspace manipulation on openshift.io. Data gaind by each run are saved in zabbix. It is prepared to run as
performance test.

## mount-volume test

The pods are created on openshift.com and this tests verify that there are no problems with starting/stopping pods.

## che-functional-test (tests)

These tests are testing rh-che and che-starter. Soon will be deprecated and replaced by tests in rh-che repo https://github.com/redhat-developer/rh-che.