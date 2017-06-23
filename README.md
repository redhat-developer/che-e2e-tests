# Che functional tests
Che functional tests verify capabilities of Che IDE. Earlier tests were more end-to-end focused (one big use case), now it is split into several test cases and it is concepted more like functional tests.

## Prerequisites
- Running Che server on OpenShift. That means the Che server is running with configuration compatible with the runtime configuration of Che starter
- Che starter _(optional)_ - It is not necessary to run Che starter - if you don't specify any URL where Che starter is available, then it will be automatically cloned from GH and started. Otherwise Che starter located at your URL will be used

## Test execution
Tests can run locally against minishift or against a remote OpenShift instance. To run tests locally, for its successfull execution you have to modify your local Che to support Bayesian, because it won't work OOTB. To have Bayesian working locally, please set up local OpenShift instance to copy the remote instance where Bayesian is running. Otherwise, if Bayesian is not set up properly, _PomTestCase_ test class will fail.

To run tests execute `mvn clean verify` with arguments listed below to run all tests.
### Execution arguments
There are 2 ways how to run tests. Each of the following contains complete list of required and optional parameters.
**Run tests with fresh new workspace**
- _openShiftMasterURL_ - **required**; URL of OpenShift master where Che server is running
- _keycloakToken_ - **required**; token to authenticate using Keycloak; token should be without the 'Bearer' prefix
- _osioUsername_ - **required**; required if workspace use authentication in front of it (if you dont't have customized Che build)
- _osioPassword_ - **required**; required if workspace use authentication in front of it (if you dont't have customized Che build)
- _openShiftNamespace_ - **required**; project on OpenShift where Che is running; default is 'eclipse-che', but this is suitable only when running on minishift
- _cheStarterURL_ - **optional**; URL of Che starter which is called to handle Che; if not provided, Che starter is started in set up step of these tests
- _preserveWorkspace_ - **optional**, default false; set to true if you want to keep workspace after tests execution

**Run tests against an existing workspace**
- _cheWorkspaceUrl_ - **required**, URL of existing and running Che workspace
- _osioUsername_ - **required**; required if workspace use authentication in front of it (if you dont't have customized Che build)
- _osioPassword_ - **required**; required if workspace use authentication in front of it (if you dont't have customized Che build)

All before-mentioned properties are possible to set using `arquillian.xml` file. To do so, use an extension part with the qualifier `che`.:

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<arquillian xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns="http://jboss.org/schema/arquillian"
    xsi:schemaLocation="http://jboss.org/schema/arquillian
    http://jboss.org/schema/arquillian/arquillian_1_0.xsd">

  <extension qualifier="che">
    <property name="openShiftToken">youropenshifttoken</property>
    <property name="openShiftMasterURL">https://your.openshift.master.url</property>
    <property name="openShiftNamespace">your-che-namespace</property>
    ...
  </extension>
  ...
</arquillian>
~~~

