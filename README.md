# Che end-to-end tests
Tests for Che end-to-end usage with integration of che-starter.s

## Test execution
Run 'mvn clean verify' to run all tests. If you have a running Che instance which you would like to test against, pass an argument '-DcheServerURL='.

## Test structure
- first scenario (1) starts che server, (2) (a) creates a workspace (b) import vert.x project (3) run a test class and check results, (4) open a file (pom.xml), (5) do some Bayesian incompatible changes and correct it via codeAssist/contextAssist/whatever, (6) commit and push
  - for now only steps (1), (2) and partially (3) are implemented. Che-starter parts in (1) and (2) are going to be rewritten - currently is used own implementation of communication with Che server.
- second scenario (1) creates a context oriented workspace, (2) open a workspace on a specified line of a file, (3) fix a problem suggested by Bayesian, (4) commit and push
  - there is nothing done for this test case so far
