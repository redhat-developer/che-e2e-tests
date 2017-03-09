# Che end-to-end tests
Tests for Che end-to-end usage with integration of che-starter.

## Test execution
Run `mvn clean verify` with arguments listed below to run all tests. 
### Execution arguments
- _preserveWorkspace_ - **optional**, default false; set to true if you want to keep workspace after tests (for further manual work)
- _cheStarterURL_ - **required**; URL of Che starter which is called to handle Che
- _openShiftMasterURL_ - **required**; URL of OpenShift master which is passed to che-starter
- _openShiftToken_ - **required**; token to authenticate to OpenShift

## Test structure
- first scenario (1) creates a workspace and import vert.x project into it (2) run a test class and check results, (3) open a file (pom.xml), 45) do some Bayesian incompatible changes and correct it via codeAssist/contextAssist/whatever, (5) commit and push, utilize PR plugin
  - steps 1 and 2 are implemented. Bayesian support and PR plugin are still not in Che, once those plugins are there, further work will be done.
- second scenario (1) creates a context oriented workspace, (2) open a workspace on a specified line of a file, (3) fix a problem suggested by Bayesian, (4) commit and push
  - there is nothing done for this test case so far
