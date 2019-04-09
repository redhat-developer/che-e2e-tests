import json, os, threading
from locust import HttpLocust, TaskSet, task, events
from datetime import datetime
import time

cheServerUrl = os.getenv("CHE_SERVER_URL")
bodyJson = '{\
    "commands": [\
        {\
            "commandLine": "scl enable rh-maven33 \u0027mvn compile vertx:debug -f ${current.project.path}\u0027",\
            "name": "debug",\
            "type": "custom",\
            "attributes": {\
                "previewUrl": "http://${server.port.8080}",\
                "goal": "Debug"\
            }\
        },\
        {\
            "commandLine": "scl enable rh-maven33 \u0027mvn compile vertx:run -f ${current.project.path}\u0027",\
            "name": "run",\
            "type": "custom",\
            "attributes": {\
                "previewUrl": "http://${server.port.8080}",\
                "goal": "Run"\
            }\
        },\
        {\
            "commandLine": "scl enable rh-maven33 \u0027mvn clean install -f ${current.project.path}\u0027",\
            "name": "build",\
            "type": "mvn",\
            "attributes": {\
                "previewUrl": "",\
                "goal": "Build"\
            }\
        }\
    ],\
    "defaultEnv": "default",\
    "description": "mycustomdescription",\
    "environments": {\
        "default": {\
            "recipe": {\
                "type": "dockerimage",\
                "location": "registry.devshift.net/che/vertx"\
            },\
            "machines": {\
                "dev-machine": {\
                    "agents": [\
                        "com.redhat.bayesian.lsp",\
                        "org.eclipse.che.ws-agent",\
                        "org.eclipse.che.terminal"\
                    ],\
                    "attributes": {\
                        "memoryLimitBytes": "2147483648"\
                    }\
                }\
            }\
        }\
    },\
    "name": "WORKSPACE_NAME",\
    "links": [],\
    "projects": [\
        {\
            "name": "vertx-http-booster",\
            "type": "maven",\
            "description": "Created via che-starter API",\
            "path": "/vertx-http-booster",\
            "source": {\
                "parameters": {\
                    "keepVcs": "true",\
                    "branch": "master"\
                },\
                "type": "git",\
                "location": "https://github.com/openshiftio-vertx-boosters/vertx-http-booster"\
            },\
            "links": [],\
            "mixins": [\
                "git"\
            ]\
        }\
    ]\
}'

_users = -1
_userTokens = []
_userEnvironment = []
_userNames = []
_currentUser = 0
_userLock = threading.RLock()

usenv = os.getenv("USER_TOKENS")
lines = usenv.split('\n')

_users = len(lines)

for u in lines:
  up = u.split(';')
  _userTokens.append(up[0])
  _userNames.append(up[1])
  _userEnvironment.append(up[2])


class TokenBehavior(TaskSet):
  taskUser = -1
  taskUserId = ""
  taskUserName = ""
  taskUserToken = ""
  taskUserEnvironment = ""
  id = ""
  openshiftToken = ""
  cluster = ""

  def on_start(self):
    global _currentUser, _users, _userLock, _userTokens, _userEnvironment, _userNames
    _userLock.acquire()
    self.taskUser = _currentUser
    if _currentUser < _users - 1:
      _currentUser += 1
    else:
      _currentUser = 0
    _userLock.release()
    self.taskUserToken = _userTokens[self.taskUser]
    self.taskUserName = _userNames[self.taskUser]
    self.taskUserEnvironment = _userEnvironment[self.taskUser]
    print("Username:" + self.taskUserName
          # + " Token:" + self.taskUserToken
          + " Environment:" + self.taskUserEnvironment)
    self.setOsTokenAndCluster()

  def setOsTokenAndCluster(self):
    # self.log("Getting info about user ")
    # username = self.taskUserName
    # Set URLs based on environment
    if "prod-preview" in self.taskUserEnvironment:
      userInfoURL = "https://auth.prod-preview.openshift.io/api/userinfo"
      usersURL = "https://api.prod-preview.openshift.io/api/users?filter[username]="
    else:
      userInfoURL = "https://auth.openshift.io/api/userinfo"
      usersURL = "https://api.openshift.io/api/users?filter[username]="
    # if "@" in self.taskUserName:
    #   userInfo = self.client.get(userInfoURL,
    #                              headers={
    #                                "Authorization": "Bearer " + self.taskUserToken},
    #                              name="getUsername", catch_response=True)
    #   username = (userInfo.json())['preferred_username']
    # self.taskUserName = username
    infoResponse = self.client.get(
        usersURL + self.taskUserName,
        name="getUserInfo", catch_response=True)
    infoResponseJson = infoResponse.json()
    self.cluster = infoResponseJson['data'][0]['attributes']['cluster']
    # os_token_response = self.client.get(
    #     "https://auth.openshift.io/api/token?for=" + self.cluster,
    #     headers={"Authorization": "Bearer " + self.taskUserToken},
    #     name="getOpenshiftToken", catch_response=True)
    # os_token_response_json = os_token_response.json()
    # self.openshiftToken = os_token_response_json["access_token"]
    self.openshiftToken = self.taskUserToken

  @task
  def createStartDeleteWorkspace(self):
    self.log(
        "Checking if there are some removing pods before creating and running new workspace.")
    self.waitUntilDeletingIsDone()
    id = self.createWorkspace()
    self.id = id
    self.wait()
    self._reset_timer()
    self.startWorkspace(id)
    self.wait()
    self.waitForWorkspaceToStart(id)
    self._reset_timer()
    self.stopWorkspace(id)
    self.waitForWorkspaceToStop(self.id)
    self.wait()
    self.deleteWorkspace(id)

  def createWorkspace(self):
    self.log("Creating workspace")
    now_time_ms = "%.f" % (time.time() * 1000)
    json = bodyJson.replace("WORKSPACE_NAME", now_time_ms)
    response = self.client.post("/api/workspace", headers={
      "Authorization": "Bearer " + self.taskUserToken,
      "Content-Type": "application/json"}, 
	  name="createWorkspace", data=json, catch_response=True)
    self.log("Create workspace server api response:" + str(response.ok))
    try:
      self.log("trace_check_response_ok_bool")
      if not response.ok:
        self.log("Can not create workspace: [" + response.content + "]")
        response.failure("Can not create workspace: [" + response.content + "]")
      else:
        self.log("trace_response.ok_true_get_response_json")
        resp_json = response.json()
        self.log("Workspace with id " 
		         + resp_json["id"] 
				 + " was successfully created.")
        response.success()
        self.log("trace_return_workspace_id")
        return resp_json["id"]
    except ValueError:
      self.log("trace_get_response_json_exception")
      response.failure("Got wrong response: [" + response.content + "]")

  def startWorkspace(self, id):
    self.log("Starting workspace id " + str(id))
    response = self.client.post("/api/workspace/" + id + "/runtime",
                                headers={
                                  "Authorization": "Bearer " + self.taskUserToken},
                                name="startWorkspace", catch_response=True)
    try:
      content = response.content
      if not response.ok:
        response.failure("Got wrong response: [" + content + "]")
      else:
        response.success()
    except ValueError:
      response.failure("Got wrong response: [" + content + "]")

  def waitForWorkspaceToStart(self, id):
    timeout_in_seconds = 300
    while self.getWorkspaceStatus(id) != "RUNNING":
      now = time.time()
      if now - self.start > timeout_in_seconds:
        events.request_failure.fire(request_type="REPEATED_GET",
                                    name="timeForStartingWorkspace",
                                    response_time=self._tick_timer(),
                                    exception="Workspace wasn't able to start in " + str(
                                        timeout_in_seconds) + " seconds.")
        self.log("Workspace " + id + " wasn't able to start in " + str(
            timeout_in_seconds) + " seconds.")
        return
      self.log("Workspace id " + id + " is still not in state RUNNING")
      self.wait()
    self.log("Workspace id " + id + " is RUNNING")
    events.request_success.fire(request_type="REPEATED_GET",
                                name="timeForStartingWorkspace",
                                response_time=self._tick_timer(),
                                response_length=0)

  def waitForWorkspaceToStop(self, id):
    while self.getWorkspaceStatus(id) != "STOPPED":
      self.log("Workspace id " + id + " is still not in state STOPPED")
      self.wait()
    self.log("Workspace id " + id + " is STOPPED")
    events.request_success.fire(request_type="REPEATED_GET",
                                name="timeForStoppingWorkspace",
                                response_time=self._tick_timer(),
                                response_length=0)

  def stopWorkspace(self, id):
    self.log("Stopping workspace id " + id)
    status = self.getWorkspaceStatus(id)
    if status == "STOPPED":
      self.log("Workspace " + id + "  is already stopped.")
      return
    response = self.client.delete("/api/workspace/" + id + "/runtime", headers={
      "Authorization": "Bearer " + self.taskUserToken}, name="stopWorkspace",
                                  catch_response=True)
    try:
      content = response.content
      if not response.ok:
        response.failure("Got wrong response: [" + content + "]")
      else:
        response.success()
    except ValueError:
      response.failure("Got wrong response: [" + content + "]")

  def deleteWorkspace(self, id):
    self.log("Deleting workspace id " + id)
    response = self.client.delete("/api/workspace/" + id, headers={
      "Authorization": "Bearer " + self.taskUserToken}, name="deleteWorkspace",
                                  catch_response=True)
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
    clusterSubstring = (self.cluster.split("."))[1]
    username = self.taskUserName
    getPodsResponse = self.client.get(
        "https://console." + clusterSubstring + ".openshift.com/api/v1/namespaces/" + username + "-che/pods",
        headers={"Authorization": "Bearer " + self.openshiftToken},
        name="getPods", catch_response=True)
    podsJson = getPodsResponse.json();
    while "rm-" in str(podsJson):
      rmpods = str(podsJson).count("rm-") / 7
      print "There are still removing pods running. Trying again after " + str(
          delay) + " seconds."
      print "Number of removing pods running: " + str(rmpods)
      time.sleep(delay)
      getPodsResponse = self.client.get(
          "https://console." + clusterSubstring + ".openshift.com/api/v1/namespaces/" + username + "-che/pods",
          headers={"Authorization": "Bearer " + self.openshiftToken},
          name="getPods", catch_response=True)
      podsJson = getPodsResponse.json();
    events.request_success.fire(request_type="REPEATED_GET",
                                name="timeForRemovingPod",
                                response_time=self._tick_timer(),
                                response_length=0)
    self.log("All removing pods finished.")

  def getWorkspaceStatus(self, id):
    response = self.client.get("/api/workspace/" + id, headers={
      "Authorization": "Bearer " + self.taskUserToken},
                               name="getWorkspaceStatus", catch_response=True)
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
      "Authorization": "Bearer " + self.taskUserToken}, name="getWorkspaces",
                               catch_response=True)
    try:
      resp_json = response.json()
      content = response.content
      if not response.ok:
        response.failure("Got wrong response: [" + content + "]")
      else:
        response.success()
        self.log("Removing " + str(len(resp_json)) + " existing workspaces.")
        for wkspc in resp_json:
          id = wkspc["id"]
          if wkspc["status"] != "STOPPED":
            self.stopWorkspace(id)
            self.waitForWorkspaceToStop(id)
          self.deleteWorkspace(id)
    except ValueError:
      response.failure("Got wrong response: [" + content + "]")

  def log(self, message):
    print self.taskUserName + ": " + message


class TokenUser(HttpLocust):
  host = cheServerUrl
  task_set = TokenBehavior
  min_wait = 1000
  max_wait = 10000
