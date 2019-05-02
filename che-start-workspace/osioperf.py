import json, os, threading
from locust import Locust, HttpLocust, TaskSet, task, events
from locust.clients import HttpSession
from locust.exception import StopLocust
from datetime import datetime
import time

che6BodyJson = '''
{
    "commands": [
        {
            "commandLine": "scl enable rh-maven33 \u0027mvn compile vertx:debug -f ${current.project.path}\u0027",
            "name": "debug",
            "type": "custom",
            "attributes": {
                "previewUrl": "http://${server.port.8080}",
                "goal": "Debug"
            }
        },
        {
            "commandLine": "scl enable rh-maven33 \u0027mvn compile vertx:run -f ${current.project.path}\u0027",
            "name": "run",
            "type": "custom",
            "attributes": {
                "previewUrl": "http://${server.port.8080}",
                "goal": "Run"
            }
        },
        {
            "commandLine": "scl enable rh-maven33 \u0027mvn clean install -f ${current.project.path}\u0027",
            "name": "build",
            "type": "mvn",
            "attributes": {
                "previewUrl": "",
                "goal": "Build"
            }
        }
    ],
    "defaultEnv": "default",
    "description": "mycustomdescription",
    "environments": {
        "default": {
            "recipe": {
                "type": "dockerimage",
                "location": "registry.devshift.net/che/vertx"
            },
            "machines": {
                "dev-machine": {
                    "agents": [
                        "com.redhat.bayesian.lsp",
                        "org.eclipse.che.ws-agent",
                        "org.eclipse.che.terminal"
                    ],
                    "attributes": {
                        "memoryLimitBytes": "2147483648"
                    }
                }
            }
        }
    },
    "name": "WORKSPACE_NAME",
    "links": [],
    "projects": [
        {
            "name": "vertx-http-booster",
            "type": "maven",
            "description": "Created via che-starter API",
            "path": "/vertx-http-booster",
            "source": {
                "parameters": {
                    "keepVcs": "true",
                    "branch": "master"
                },
                "type": "git",
                "location": "https://github.com/openshiftio-vertx-boosters/vertx-http-booster"
            },
            "links": [],
            "mixins": [
                "git"
            ]
        }
    ]
}
'''

che7BodyJson = '''
{
  "projects": [
    {
      "mixins": [],
      "problems": [],
      "source": {
        "location": "https://github.com/che-samples/web-nodejs-sample.git",
        "type": "git",
        "parameters": {}
      },
      "description": "Simple NodeJS Project.",
      "name": "nodejs-hello-world",
      "type": "node-js",
      "path": "/nodejs-hello-world",
      "attributes": {
        "language": [
          "javascript"
        ]
      }
    }
  ],
  "commands": [
    {
      "commandLine": "echo ${CHE_OSO_CLUSTER//api/console}",
      "name": "Get OpenShift Console URL",
      "type": "custom",
      "attributes": {}
    },
    {
      "commandLine": "cd ${current.project.path} \n node .",
      "name": "nodejs-hello-world:run",
      "type": "custom",
      "attributes": {
        "goal": "Run",
        "previewUrl": "${server.3000/tcp}"
      }
    }
  ],
  "defaultEnv": "default",
  "environments": {
    "default": {
      "recipe": {
        "contentType": "application/x-yaml",
        "type": "openshift",
        "content": "kind: List\nitems:\n - \n  apiVersion: v1\n  kind: Pod\n  metadata:\n   name: ws\n  spec:\n   containers:\n    - \n     image: 'eclipse/che-dev:nightly'\n     name: dev\n     resources:\n      limits:\n       memory: 512Mi\n"
      },
      "machines": {
        "ws/dev": {
          "servers": {},
          "volumes": {
            "projects": {
              "path": "/projects"
            }
          },
          "installers": [],
          "env": {},
          "attributes": {
            "memoryLimitBytes": "536870912"
          }
        }
      }
    }
  },
  "name": "WORKSPACE_NAME",
  "attributes": {
    "plugins": "che-machine-exec-plugin:0.0.1",
    "editor": "org.eclipse.che.editor.theia:1.0.0"
  }
}
'''

_users = -1
_userTokens = []
_userEnvironment = []
_userNames = []
_currentUser = 0
_userLock = threading.RLock()


class TokenBehavior(TaskSet):
  id = ""
  openshiftToken = ""
  cluster = ""
  cycles = 0
  cyclesMax = 1

  def on_start(self):
    self.log("Username:" + self.locust.taskUserName
            #  + " Token:" + self.taskUserToken
             + " Environment:" + self.locust.taskUserEnvironment)
    if (os.getenv("CYCLES_COUNT") != None):
      self.cyclesMax = int(os.getenv("CYCLES_COUNT"))
    self.setOsTokenAndCluster()
    self.deleteExistingWorkspaces()

  def setOsTokenAndCluster(self):
    self.log("Getting info about user ")
    username = self.locust.taskUserName
    # Set URLs based on environment
    if "prod-preview" in self.locust.taskUserEnvironment:
      userInfoURL = "https://auth.prod-preview.openshift.io/api/userinfo"
      usersURL = "https://api.prod-preview.openshift.io/api/users?filter[username]="
    else:
      userInfoURL = "https://auth.openshift.io/api/userinfo"
      usersURL = "https://api.openshift.io/api/users?filter[username]="
    if "@" in self.locust.taskUserName:
      userInfo = self.client.get(userInfoURL,
                                 headers={
                                   "Authorization": "Bearer " + self.locust.taskUserToken},
                                 name="getUsername", catch_response=True)
      username = (userInfo.json())['preferred_username']
    self.locust.taskUserName = username
    infoResponse = self.client.get(
        usersURL + self.locust.taskUserName,
        name="getUserInfo", catch_response=True)
    infoResponseJson = infoResponse.json()
    self.cluster = infoResponseJson['data'][0]['attributes']['cluster']
    self.clusterName = self.cluster.split(".")[1]
    os_token_response = self.client.get(
        "https://auth.openshift.io/api/token?for=" + self.cluster,
        headers={"Authorization": "Bearer " + self.locust.taskUserToken},
        name="getOpenshiftToken", catch_response=True)
    os_token_response_json = os_token_response.json()
    self.openshiftToken = os_token_response_json["access_token"]

  @task
  def createStartDeleteWorkspace(self):
    print("\n["+self.clusterName+"] Running workspace start test "+str(self.cycles + 1)+" of "+str(self.cyclesMax)+"\n")
    self.log("Checking if there are some removing pods before creating and running new workspace.")
    self.waitUntilDeletingIsDone()
    self.id = self.createWorkspace()
    self.wait()
    self._reset_timer()
    self.startWorkspace()
    self.wait()
    self.waitForWorkspaceToStart()
    self._reset_timer()
    self.stopWorkspaceSelf()
    self.waitForWorkspaceToStopSelf()
    self.wait()
    self.deleteWorkspaceSelf()
    if (self.cycles == (self.cyclesMax - 1)):
      raise StopLocust("Tests finished, unable to set Locust to run set number of times (https://github.com/locustio/locust/pull/656), issuing hard-stop.")
    self.cycles += 1

  def createWorkspace(self):
    self.log("Creating workspace")
    now_time_ms = "%.f" % (time.time() * 1000)
    json = che7BodyJson.replace("WORKSPACE_NAME", now_time_ms)
    response = self.client.post("/api/workspace", headers={
      "Authorization": "Bearer " + self.locust.taskUserToken,
      "Content-Type": "application/json"}, 
     name="createWorkspace_"+self.clusterName, data=json, catch_response=True)
    self.log("Create workspace server api response:" + str(response.ok))
    try:
      if not response.ok:
        self.log("Can not create workspace: [" + response.content + "]")
        response.failure("Can not create workspace: [" + response.content + "]")
      else:
        resp_json = response.json()
        self.log("Workspace with id " 
                 + resp_json["id"] 
                 + " was successfully created.")
        response.success()
        return resp_json["id"]
    except ValueError:
      response.failure("Got wrong response: [" + response.content + "]")

  def startWorkspace(self):
    self.log("Starting workspace id " + str(self.id))
    response = self.client.post("/api/workspace/" + self.id + "/runtime",
                                headers={
                                  "Authorization": "Bearer " + self.locust.taskUserToken},
                                name="startWorkspace_"+self.clusterName, catch_response=True)
    try:
      content = response.content
      if not response.ok:
        response.failure("Got wrong response: [" + content + "]")
      else:
        response.success()
    except ValueError:
      response.failure("Got wrong response: [" + content + "]")

  def waitForWorkspaceToStart(self):
    timeout_in_seconds = 300
    workspace_status = self.getWorkspaceStatusSelf()
    while workspace_status != "RUNNING":
      now = time.time()
      if now - self.start > timeout_in_seconds:
        events.request_failure.fire(request_type="REPEATED_GET",
                                    name="timeForStartingWorkspace_"+self.clusterName,
                                    response_time=self._tick_timer(),
                                    exception="Workspace wasn't able to start in " 
                                              + str(timeout_in_seconds)
                                              + " seconds.")
        self.log("Workspace " + self.id + " wasn't able to start in " 
                 + str(timeout_in_seconds) + " seconds.")
        return
      self.log("Workspace id " + self.id + " is still not in state RUNNING ["+ workspace_status +"]")
      self.wait()
      workspace_status = self.getWorkspaceStatusSelf()
    self.log("Workspace id " + self.id + " is RUNNING")
    events.request_success.fire(request_type="REPEATED_GET",
                                name="timeForStartingWorkspace_"+self.clusterName,
                                response_time=self._tick_timer(),
                                response_length=0)

  def waitForWorkspaceToStopSelf(self):
    self.waitForWorkspaceToStop(self.id)

  def waitForWorkspaceToStop(self, id):
    workspace_status = self.getWorkspaceStatus(id)
    while workspace_status != "STOPPED":
      self.log("Workspace id " + id + " is still not in state STOPPED ["+ workspace_status +"]")
      self.wait()
      workspace_status = self.getWorkspaceStatus(id)
    self.log("Workspace id " + id + " is STOPPED")
    events.request_success.fire(request_type="REPEATED_GET",
                                name="timeForStoppingWorkspace_"+self.clusterName,
                                response_time=self._tick_timer(),
                                response_length=0)

  def stopWorkspaceSelf(self):
    return self.stopWorkspace(self.id)

  def stopWorkspace(self, id):
    self.log("Stopping workspace id " + id)
    status = self.getWorkspaceStatus(id)
    if status == "STOPPED":
      self.log("Workspace " + id + "  is already stopped.")
      return
    response = self.client.delete("/api/workspace/" + id + "/runtime", headers={
                                    "Authorization": "Bearer " + self.locust.taskUserToken},
                                  name="stopWorkspace_"+self.clusterName, catch_response=True)
    try:
      content = response.content
      if not response.ok:
        response.failure("Got wrong response: [" + content + "]")
      else:
        response.success()
    except ValueError:
      response.failure("Got wrong response: [" + content + "]")

  def deleteWorkspaceSelf(self):
    self.deleteWorkspace(self.id)

  def deleteWorkspace(self, id):
    self.log("Deleting workspace id " + id)
    response = self.client.delete("/api/workspace/" + id, headers={
                                    "Authorization": "Bearer " + self.locust.taskUserToken},
                                  name="deleteWorkspace_"+self.clusterName, catch_response=True)
    try:
      content = response.content
      if not response.ok:
        response.failure("Got wrong response: [" + content + "]")
      else:
        response.success()
    except ValueError:
      response.failure("Got wrong response: [" + content + "]")

  def waitUntilDeletingIsDone(self):
    self._reset_timer()
    delay = 10
    failcount = 0
    clusterSubstring = (self.cluster.split("."))[1]
    getPodsResponse = self.client.get(
        "https://console." + clusterSubstring + ".openshift.com/api/v1/namespaces/" + self.locust.taskUserName + "-che/pods",
        headers={"Authorization": "Bearer " + self.openshiftToken},
        name="getPods-"+self.cluster, catch_response=True)
    podsJson = getPodsResponse.json()
    while "rm-" in str(podsJson):
      rmpods = str(podsJson).count("rm-") / 7
      self.log("There are still removing pods running. Trying again after " 
            + str(delay) + " seconds.")
      self.log("Number of removing pods running: " + str(rmpods))
      time.sleep(delay)
      getPodsResponse = self.client.get(
          "https://console." + clusterSubstring + ".openshift.com/api/v1/namespaces/" + self.locust.taskUserName + "-che/pods",
          headers={"Authorization": "Bearer " + self.openshiftToken},
          name="getPods_"+self.clusterName, catch_response=True)
      podsJson = getPodsResponse.json()
      failcount += 1
      # After waiting for a minute, stop the locust test with generating the results
      if (failcount >= 6):
        raise StopLocust("The remove pod failed to finish execution within a minute. Stopping locust thread.")
    events.request_success.fire(request_type="REPEATED_GET",
                                name="timeForRemovingPod_"+self.clusterName,
                                response_time=self._tick_timer(),
                                response_length=0)
    self.log("All removing pods finished.")

  def getWorkspaceStatusSelf(self):
    return self.getWorkspaceStatus(self.id)

  def getWorkspaceStatus(self, id):
    response = self.client.get("/api/workspace/" + id, headers={
      "Authorization": "Bearer " + self.locust.taskUserToken},
                               name="getWorkspaceStatus_"+self.clusterName, catch_response=True)
    try:
      resp_json = response.json()
      content = response.content
      if not response.ok:
        response.failure("Got wrong response: [" + content + "]")
      else:
        response.success()
        return resp_json["status"]
    except ValueError:
      response.failure("Got wrong response: [" + content + "]")

  def _reset_timer(self):
    self.start = time.time()

  def _tick_timer(self):
    self.stop = time.time()
    ret_val = (self.stop - self.start) * 1000
    return ret_val

  def deleteExistingWorkspaces(self):
    response = self.client.get("/api/workspace/", headers={
                               "Authorization": "Bearer " + self.locust.taskUserToken},
                               name="getWorkspaces_"+self.clusterName, catch_response=True)
    try:
      resp_json = response.json()
      content = response.content
      if not response.ok:
        response.failure("Got wrong response: [" + content + "]")
      else:
        response.success()
        self.log("Removing " + str(len(resp_json)) + " existing workspaces.")
        for wkspc in resp_json:
          wkspid = wkspc["id"]
          if wkspc["status"] != "STOPPED":
            self.stopWorkspace(wkspid)
            self.waitForWorkspaceToStop(wkspid)
          self.deleteWorkspace(wkspid)
    except ValueError:
      response.failure("Got wrong response: [" + content + "]")

  def log(self, message):
    print(self.locust.taskUserName + ": " + message)


class OsioperfLocust(Locust):
  taskUser = -1
  taskUserName = ""
  taskUserToken = ""
  taskUserEnvironment = ""

  def __init__(self, *args, **kwargs):
    global _currentUser, _userLock, _users, _userTokens, _userEnvironment, _userNames
    super(Locust, self)
    TOKEN_INDEX = 0
    USERNAME_INDEX = 1
    ENVIRONMENT_INDEX = 2
    usenv = os.getenv("USER_TOKENS")
    lines = usenv.split('\n')
    _users = len(lines)
    for u in lines:
      up = u.split(';')
      _userTokens.append(up[TOKEN_INDEX])
      _userNames.append(up[USERNAME_INDEX])
      _userEnvironment.append(up[ENVIRONMENT_INDEX])
    # Async lock user to prevent two threads runing with the same user
    _userLock.acquire()
    self.taskUser = _currentUser
    self.taskUserToken = _userTokens[_currentUser]
    self.taskUserName = _userNames[_currentUser]
    self.taskUserEnvironment = _userEnvironment[_currentUser]
    print("Spawning user ["+str(self.taskUser)+"] on ["+self.taskUserEnvironment+"]")
    if _currentUser < _users - 1:
      _currentUser += 1
    else:
      _currentUser = 0
    _userLock.release()
    # User lock released, critical section end
    host = "https://che.prod-preivew.openshift.io" if "prod-preview" in self.taskUserEnvironment else "https://che.openshift.io"
    self.client = HttpSession(base_url=host)

class TokenUser(OsioperfLocust):
  task_set = TokenBehavior
  min_wait = 1000
  max_wait = 10000
