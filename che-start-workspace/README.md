# Workspace Services Performance Evaluation
These tests are intended to measure performance of the REST endpoints of creating/starting/stopping/deleting workspace. It serves as monitoring of service on daily basis.

## Environment
The tested server is the OSIO (https://che.openshift.io or https://rhche.openshift.io).


## Test setup
The test in the environment is executed with 2 tested OSIO user accounts that has a GitHub account linked.
Although user accounts can be spread between individual client nodes of the OsioPerf Lab, for two users it is sufficient to run the test only on master node.
Each simulated user sends request for creating workspace,
waits until the workspace is running, then stops the workspace and deletes it.

The whole performance test suite is executed regularly once a day
while a run of single loop (create/start/wait/stop/delete) takes approximately 1.5 minutes. The summary results of each run
are archived for 28 days by the private Jenkins monitoring system
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
where `state` is generated unique UUID v4 wait for the `LOG IN` button to be clickable which indicates that the page is loaded.
(in `/login` it is upto us to generate `state`, thus we use uuid, but in `/authorize` state is generated and sent by client,
we don't have a restriction on that. `state` sent by client could be any string, but since state is supposed to be random enough and unique,
uuid would be a good choice. But it is upto client to use it or not)

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

### Load phase
Users try to create workspace specified by json. If workspace is created, user waits until it is started and running. Then the workspace is
stopped and deleted. Whole process is repeated. When time for tests runs out, all remaining workspaces are deleted.

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
Waiting until workspace status changes to RUNNING.

HTTP client keeps sending these requests until the status of workspace is running. Time of each GET request is measured
(getWorksapceStatus metrics) and time needed to start workspace is measured too (timeForStartingWorksapce metrics).

#### *Stopping workspace* (`stopWorkspace`)
Using HTTP client send:
```
DELETE /api/workspace/<workspace_id>/runtime
```

#### *Waiting until workspace stops* (`timeForStoppingWorkspace` and `getWorkspaceStatus`)
Using HTTP client repeatedly send:
```
GET /api/worksapce/<worksapce_id>
```
Than sending request for getting status and measuring time until the status is STOPPED.

#### *Deleting workspace* (`deleteWorkspace`)
Using HTTP client send:
```
DELETE /api/workspace/<workspace_id>
```

## How to run the tests locally
By default the load test is executed by Locust tool running locally (for two users it is not necesary to run in a distributed mode).

So the environment variable is set `RUN_LOCALLY=true`.

To run the test, configure the test in `_setenv.sh` file and run the `run.sh` script.

To update jenkins job use this syntax (not working for last version of jenkins job builder):
```
sudo PYTHONHTTPSVERIFY=0 jenkins-jobs --conf jenkins_jobs.ini update job.yml
```
and update jenkins_job.ini with your credentials.

## Results

Results of tests are sent to zabbix to create graphs. Artifacts as csv, png and log files are saved for each job on private Jenkins.

## Dependencies

There are two subproject which are cloned from git: common.git and openshift.loginusers.git

### common

This repo is responsible for test execution and environment preparation. It handles locust and generate logs and send logs to zabbix.

Located here: https://github.com/pmacik/openshiftio-performance-common/

### openshift.loginuser

At the begining of test users have to be logged in to obtain their active/refresh tokens. This repo contains two possible approaches
to login users. Each generates file with needed credentials.

Located here: https://github.com/pmacik/openshiftio-loginusers