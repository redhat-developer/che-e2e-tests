# Tests for mounting volume
These tests try to mount volume for pod on openshift in namespace named \<username\>. Tests are executed via Jenkins in one hour intervals.
Results are collected in zabbix.
Both Jenkins and zabbix are in private network.


## Prerequisites
- Running OpenShift

## Test execution
There is file run_test.sh which is executing tests. Tests are run against openshift so you need to pass variables specifying login details.

`./run_test.sh <username> <user_password> <url of openshift>`

This script expects three environment variables set:

| Variable | description |
| ------------- |-------------|
| MAX_COUNT | maximum count of tries for mounting pod |
| ZABBIX_SERVER | server of zabbix where results are stored |
| ZABBIX_HOST | zabbix host for storing results |


After test run, the file zabbix.log is generated. This file includes metrics which are sent to zabbix. This file is also saved as an artifact of the jenkins job.

## Scenarios

At the begining of test, the oc and jq are downloaded and PATH is set. After that, test itself runs in loop for MAX_COUNT times.

Each loop tries to create pod, wait until it is started, stop it and waits until it is deleted. The time for each operation is measured and send via
zabbix_sender to zabbix.