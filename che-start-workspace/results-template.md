# Results (@@JOB_BASE_NAME@@ #@@BUILD_NUMBER@@ @@TIMESTAMP@@)
## Summary
The following tables show the final results taken and computed from a single test run - repeatedly created/started/deleted workspace.

The summary results of each run are uploaded to the
[Zabbix](https://zabbix.devshift.net:9443/zabbix/screens.php?elementid=32&fullscreen=1)
monitoring system to track the results' history.

### Workspace Test
| Scenario | Minimal | Median | Maximal |
| :--- | ---: | ---: | ---: |
| `createWorkspace` | @@CREATE_WORKSPACE_MIN@@ ms | @@CREATE_WORKSPACE_MEDIAN@@ ms | @@CREATE_WORKSPACE_MAX@@ ms |
| `startWorkspace` | @@START_WORKSPACE_MIN@@ ms | - | @@START_WORKSPACE_MEDIAN@@ ms | @@START_WORKSPACE_MAX@@ ms |
| `getWorkspaceStatus` | @@GET_WORKSPACE_STATUS_MIN@@ ms | @@GET_WORKSPACE_STATUS_MEDIAN@@ ms | @@GET_WORKSPACE_STATUS_MAX@@ ms |
| `timeForStartingWorkspace` | @@TIME_FOR_STARTING_WORKSPACE_MIN@@ ms | @@TIME_FOR_STARTING_WORKSPACE_MEDIAN@@ ms | @@TIME_FOR_STARTING_WORKSPACE_MAX@@ ms |
| `stopWorkspace` | @@STOP_WORKSPACE_MIN@@ ms | @@STOP_WORKSPACE_MEDIAN@@ ms | @@STOP_WORKSPACE_MAX@@ ms |
| `deleteWorkspace` | @@DELETE_WORKSPACE_MIN@@ ms | @@DELETE_WORKSPACE_MEDIAN@@ ms | @@DELETE_WORKSPACE_MAX@@ ms |

## Test charts
The charts bellow show the whole duration of all the phases for each scenario - i.e. what lead to the final results shown above in the summary.

### Worksapce Test

#### `createWorkspace` Response Time
![createWorkspace-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-POST_createWorkspace-response-time.png)
![createWorkspace-minimal-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-POST_createWorkspace-minimal-response-time.png)
![createWorkspace-median-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-POST_createWorkspace-median-response-time.png)
![createWorkspace-maximal-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-POST_createWorkspace-maximal-response-time.png)
![createWorkspace-average-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-POST_createWorkspace-average-response-time.png)
![createWorkspace-rt-histo](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-POST_createWorkspace-rt-histo.png)

#### `startWorkspace` Response Time
![startWorkspace-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-POST_startWorkspace-response-time.png)
![startWorkspace-minimal-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-POST_startWorkspace-minimal-response-time.png)
![startWorkspace-median-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-POST_startWorkspace-median-response-time.png)
![startWorkspace-maximal-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-POST_startWorkspace-maximal-response-time.png)
![startWorkspace-average-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-POST_startWorkspace-average-response-time.png)
![startWorkspace-rt-histo](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-POST_startWorkspace-rt-histo.png)

#### `timeForStartingWorkspace` Response Time
![timeForStartingWorkspace-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-REPEATED_GET_timeForStartingWorkspace-response-time.png)
![timeForStartingWorkspace-minimal-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-REPEATED_GET_timeForStartingWorkspace-minimal-response-time.png)
![timeForStartingWorkspace-median-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-REPEATED_GET_timeForStartingWorkspace-median-response-time.png)
![timeForStartingWorkspace-maximal-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-REPEATED_GET_timeForStartingWorkspace-maximal-response-time.png)
![timeForStartingWorkspace-average-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-REPEATED_GET_timeForStartingWorkspace-average-response-time.png)
![timeForStartingWorkspace-rt-histo](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-REPEATED_GET_timeForStartingWorkspace-rt-histo.png)

#### `getWorkspaceStatus` Response Time
![getWorkspaceStatus-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-GET_getWorkspaceStatus-response-time.png)
![getWorkspaceStatus-minimal-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-GET_getWorkspaceStatus-minimal-response-time.png)
![getWorkspaceStatus-median-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-GET_getWorkspaceStatus-median-response-time.png)
![getWorkspaceStatus-maximal-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-GET_getWorkspaceStatus-maximal-response-time.png)
![getWorkspaceStatus-average-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-GET_getWorkspaceStatus-average-response-time.png)
![getWorkspaceStatus-rt-histo](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-GET_getWorkspaceStatus-rt-histo.png)


#### `stopWorkspace` Response Time
![stopWorkspace-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-DELETE_stopWorkspace-response-time.png)
![stopWorkspace-minimal-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-DELETE_stopWorkspace-minimal-response-time.png)
![stopWorkspace-median-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-DELETE_stopWorkspace-median-response-time.png)
![stopWorkspace-maximal-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-DELETE_stopWorkspace-maximal-response-time.png)
![stopWorkspace-average-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-DELETE_stopWorkspace-average-response-time.png)
![stopWorkspace-rt-histo](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-DELETE_stopWorkspace-rt-histo.png)


#### `deleteWorkspace` Response Time
![deleteWorkspace-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-DELETE_deleteWorkspace-response-time.png)
![deleteWorkspace-minimal-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-DELETE_deleteWorkspace-minimal-response-time.png)
![deleteWorkspace-median-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-DELETE_deleteWorkspace-median-response-time.png)
![deleteWorkspace-maximal-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-DELETE_deleteWorkspace-maximal-response-time.png)
![deleteWorkspace-average-reponse-time](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-DELETE_deleteWorkspace-average-response-time.png)
![deleteWorkspace-rt-histo](./@@JOB_BASE_NAME@@-@@BUILD_NUMBER@@-DELETE_deleteWorkspace-rt-histo.png)

