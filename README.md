# Che functional tests
Che functional tests verify capabilities of Che IDE. Earlier tests were more end-to-end focused (one big use case), now it is split into several test cases and it is concepted more like functional tests.

## Prerequisites
- Running Che server on OpenShift. That means the Che server is running with configuration compatible with the runtime configuration of Che starter
- Che starter _(optional)_ - It is not necessary to run Che starter - if you don't specify any URL where Che starter is available, then it will be automatically cloned from GH and started. Otherwise Che starter located at your URL will be used

## Test execution
Due to some changes to che-starter, tests can run only against a remote OpenShift instance. OpenShift token of an instance has to be obtainable via REST call against a Keycloak (with keycloak token). 

If you still want to run tests locally, you would have to create a new workspace manually and run tests against the workspace.

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

## Known bugs and troubleshooting
- [Tests fail if Che server is idle](https://github.com/redhat-developer/che-functional-tests/issues/62) - if tests fail in che-selenium extension (response issues in WorkspaceManager), it could be caused by Che server idling. Rerun tests once Che server starts.
