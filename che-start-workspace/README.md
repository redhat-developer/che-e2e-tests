# Che.openshift.io Workspace Services Performance Evaluation
These tests are intended to measure performance of the REST endpoints of creating/starting/stopping/deleting workspace.

## Environment
The tested server is the OSIO (https://che.openshift.io).
The clients to the tested server are deployed on the client nodes 
of the [OsioPerf Lab](https://github.com/redhat-developer/che-functional-tests/che-start-workspace/README.md).

## Test setup
The test in the environment is executed with 2 tested OSIO user accounts that has a GitHub account linked.
The user accounts are evenly spread between 2 individual client nodes of the OsioPerf Lab
from whose the requests are sent via 2 simultaneous clients. Each simulated user sends request for creating workspace,
waits until the workspace is running, then stops the workspace and deletes it.

The whole performance test suite is executed regularly once a day
while a single run takes approximately 1.5 minutes. The summary results of each run
are archived for 28 days by the [Jenkins](https://osioperf-jenkins.rhev-ci-vms.eng.rdu2.redhat.com/view/Che/job/che-start-workspace/<build_number>/artifact/che-start-workspace/) monitoring system
to track the results' history. 

## Scenarios
The performance test suite is divided into two phases of testing:
 * *Prepare* - where each test user is logged in via UI once.
 * *Load* - where requests are sent to the tested endpoints repeatedly, while the response time is measured. 

### Prepare phase
Executed once per user to get user’s tokens, ID and name before the load test begins.
This is necessary to obtain access tokens for the requests to the secured endpoints.

#### OAuth2 Friendly Login
##### *Open login page*
From
```
GET /api/authorize?response_type=code&client_id=740650a2-9c44-4db5-b067-a3d1b2cd2d01&scope=user:email&state=<STATE>&redirect_uri=https://<AUTH_SERVER_HOST>/api/status
```
where `state` is generated unique UUID v4 wait for the `LOG IN` button to be clickable which indicates that the page is loaded. (in `/login` it is upto us to generate `state`, thus we use uuid, but in `/authorize` state is generated and sent by client, we don't have a restriction on that. `state` sent by client could be any string, but since state is supposed to be random enough and unique, uuid would be a good choice. But it is upto client to use it or not)

##### *Get code*
From clicking on the `LOG IN` button wait to be redirected to the `https://<AUTH_SERVER_HOST>/api/status?code=<CODE>&state=<STATE>`.

Check that the returned `<STATE>` is equal to the original and extract `<CODE>` from the URI.

##### *Get token*
Using HTTP client send:
```
POST /api/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code&client_id=740650a2-9c44-4db5-b067-a3d1b2cd2d01&code=<CODE>&redirect_uri=https://<AUTH_SERVER_HOST>/api/status
```

From the response JSON extract the `auth_token` and `refresh_token`.

##### *Login the user* (`oauth2-login-time`)
This is computed as a sum of `oauth2-get-code-time` and `oauth2-get-token-time` values.

### Load phase
Users try to create workspace specified by json. If workspace is created, user waits until it is started and running. Then the workspace is
stopped and deleted. Whole proces is repeated. When time for tests runs out, all remaining workspaces are deleted.

#### *Creating workspace* (`createWorkspace`)
Using HTTP client send:
```
POST /api/workspace
Content-Type: application/json
```

Json with specification of workspace is sent via this post request.

#### *Starting workspace* (`startWorksapce`)
When worksapce is created, HTTP client is used to start it:
```
POST /api/workspace/<worksapce_id>/runtime
Content-Type: application/json
```

#### *Waiting until workspace starts* (`timeForStartingWorkspace` and `getWorkspaceStatus`)
Using HTTP client repeatedly send:
```
GET /api/worksapce/<worksapce_id>
```

HTTP client keeps sending these requests until the status of workspace is running. Time of each GET request is measured
(getWorksapceStatus metrics) and time needed to start worksapce is measured too (timeForStartingWorksapce metrics).

#### *Stopping workspace* (`stopWorkspace`)
Using HTTP client send:
```
DELETE /api/workspace/<workspace_id>/runtime
```

#### *Deleting workspace* (`deleteWorkspace`)
Using HTTP client send:
```
DELETE /api/workspace/<workspace_id>
```

## How to run the tests locally
By default the load test is executed by Locust tool running in a distributed mode, i.e. uses remote access
to the Master and Slave nodes via SSH to start Locust process on those nodes to load the tested system
from a different places.

However, it is possible to switch the test to the local execution. To do that simply set the environment
variable `RUN_LOCALLY=true`. The easiest way is to uncomment the respective line in `_setenv.sh` file.

To run the test, configure the test in `_setenv.sh` file and run the `run.sh` script.