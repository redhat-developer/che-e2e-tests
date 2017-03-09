# Che end-to-end tests
Tests for Che end-to-end usage with integration of che-starter.s

## Test execution
Run `mvn clean verify` with arguments listed below to run all tests. 
### Execution arguments
When running locally or without che-starter, use following arguments
- _cheServerURL_ - **required**; URL of Che server which should be used for tests 
- _preserveWorkspace_ - **optional**, default false; set to true if you want to keep workspace after tests (for further manual work)

When running with che-starter, use following arguments
- _cheStarterURL_ - **required**; URL of Che starter which is called to handle Che
- _openShiftMasterURL_ - **required**; URL of OpenShift master which is passed to che-starter
- _openShiftToken_ - **required**; token to authenticate to OpenShift

## Test structure
- first scenario (1) starts che server, (2) (a) creates a workspace (b) import vert.x project (3) run a test class and check results, (4) open a file (pom.xml), (5) do some Bayesian incompatible changes and correct it via codeAssist/contextAssist/whatever, (6) commit and push
  - for now only steps (1), (2) and partially (3) are implemented. Che-starter parts in (1) and (2) are going to be rewritten - currently is used own implementation of communication with Che server.
- second scenario (1) creates a context oriented workspace, (2) open a workspace on a specified line of a file, (3) fix a problem suggested by Bayesian, (4) commit and push
  - there is nothing done for this test case so far
