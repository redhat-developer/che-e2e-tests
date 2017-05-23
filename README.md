# Che end-to-end tests
Che end-to-end tests verify capabilities of che-starter and Che IDE. 

## Prerequisites
- Running Che server on an OpenShift with properly set up Che server. That means the Che server is running with configuration compatible with the runtime configuration of the running Che starter
- Che starter - It is not necessary having it running - if you don't specify any URL where Che starter is available, then it will be automatically cloned from GH and started. You just have to install and provide certificate file for kubernetes client via propperty _kubernetes.certs.ca.file_

## Test execution
Run `mvn clean verify` with arguments listed below to run all tests.
### Execution arguments
- _preserveWorkspace_ - **optional**, default false; set to true if you want to keep workspace after tests (for further manual work)
- _cheStarterURL_ - **required**; URL of Che starter which is called to handle Che
- _openShiftMasterURL_ - **required**; URL of OpenShift master which is passed to Che starter
- _openShiftToken_ - **required if keycloakToken not present**; token to authenticate to OpenShift
- _keycloakToken_ - **required if openShiftToken not present**; token to authenticate via keycloak
- _openShiftNamespace_ - **optional**, default 'eclipse-che'; namespace on OpenShift where Che is running

Alternatively, you can run tests against an existing Che workspace using its URL
- _cheWorkspaceUrl_ - **required**, URL of existing and running Che workspace
- _osioUsername_ - **optional**; required if workspace use authentication in front of it
- _osioPassword_ - **optional**; required if workspace use authentication in front of it

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


