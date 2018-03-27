import json, os, threading
from locust import HttpLocust, TaskSet, task, events
from datetime import datetime
import time

cheServerUrl = os.getenv("CHE_SERVER_URL")
bodyJson='{\
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
_userRefreshTokens = []
_currentUser = 0
_userLock = threading.RLock()

usenv = os.getenv("USER_TOKENS")
lines = usenv.split('\n')

_users = len(lines)


for u in lines:
	up = u.split(';')
	_userTokens.append(up[0])
	_userRefreshTokens.append(up[1])

class TokenBehavior(TaskSet):

	taskUser = -1
	taskUserId = ""
	taskUserName = ""
	taskUserToken = ""
	taskUserRefreshToken = ""
	id = ""

	def on_start(self):
		events.quitting += self.on_stop
		global _currentUser, _users, _userLock, _userTokens, _userRefreshTokens
		_userLock.acquire()
		self.taskUser = _currentUser
		if _currentUser < _users - 1:
			_currentUser += 1
		else:
			_currentUser = 0
		_userLock.release()
		self.taskUserToken = _userTokens[self.taskUser]
		self.taskUserRefreshToken = _userRefreshTokens[self.taskUser]

	def on_stop(self):
		print "Running on_stop method - trying to stop and delete workspace with id " + self.id
		self.stopWorkspace(self.id)
		self.waitForWorkspaceToStop(self.id)
		self.deleteWorkspace(self.id)
		self.deleteExistingWorkspaces()

	@task
	def createStartDeleteWorkspace(self):
		id = self.createWorkspace()
		self.id = id
		print "TEST id="+id
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
		print "Creating workspace"
		now_time_ms = "%.f" % (time.time()*1000)
		print "now time:" + now_time_ms
		json = bodyJson.replace("WORKSPACE_NAME", now_time_ms)
		response = self.client.post("/api/workspace", headers = {"Authorization" : "Bearer " + self.taskUserToken, "Content-Type":"application/json"}, name = "createWorkspace", data = json, catch_response = True)

		try:
			resp_json = response.json()
			print resp_json
			if not response.ok:
				response.failure("Can not create workspace: [" + response.content + "]")
			else:
				response.success()
				return resp_json["id"]
		except ValueError:
			response.failure("Got wrong response: [" + response.content + "]")

	def startWorkspace(self, id):
		print "Starting workspace id "+id
		response = self.client.post("/api/workspace/"+id+"/runtime", headers = {"Authorization" : "Bearer " + self.taskUserToken}, name = "startWorkspace", catch_response = True)
		try:
			content = response.content
			if not response.ok:
				response.failure("Got wrong response: [" + content + "]")
			else:
				response.success()
		except ValueError:
			response.failure("Got wrong response: [" + content + "]")

	def waitForWorkspaceToStart(self, id):
		while self.getWorkspaceStatus(id) != "RUNNING":
			print "Workspace id "+id+" is still not in state RUNNING"
			self.wait()
		print "Workspace id "+id+" is RUNNING"
		events.request_success.fire(request_type="REPEATED_GET", name="timeForStartingWorkspace", response_time=self._tick_timer(), response_length=0)


	def waitForWorkspaceToStop(self, id):
		while self.getWorkspaceStatus(id) != "STOPPED":
			print "Workspace id "+id+" is still not in state STOPPED"
			self.wait()
		print "Workspace id "+id+" is STOPPED"
		events.request_success.fire(request_type="REPEATED_GET", name="timeForStoppingWorkspace", response_time=self._tick_timer(), response_length=0)


	def stopWorkspace(self, id):
		print "Stopping workspace id "+id
		response = self.client.delete("/api/workspace/"+id+"/runtime", headers = {"Authorization" : "Bearer " + self.taskUserToken}, name = "stopWorkspace", catch_response = True)
		try:
			content = response.content
			if not response.ok:
				response.failure("Got wrong response: [" + content + "]")
			else:
				response.success()
		except ValueError:
			response.failure("Got wrong response: [" + content + "]")

	def deleteWorkspace(self, id):
		print "Deleting workspace id "+id
		response = self.client.delete("/api/workspace/"+id, headers = {"Authorization" : "Bearer " + self.taskUserToken}, name = "deleteWorkspace", catch_response = True)
		try:
			content = response.content
			if not response.ok:
				response.failure("Got wrong response: [" + content + "]")
			else:
				response.success()
		except ValueError:
			response.failure("Got wrong response: [" + content + "]")

	def getWorkspaceStatus(self, id):
		response = self.client.get("/api/workspace/"+id, headers = {"Authorization" : "Bearer " + self.taskUserToken}, name = "getWorkspaceStatus", catch_response = True)
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
		self.start = self.stop
		return ret_val

	def deleteExistingWorkspaces(self):
		response = self.client.get("/api/workspace/", headers = {"Authorization" : "Bearer " + self.taskUserToken}, name = "getWorkspaces", catch_response = True)
		try:
			resp_json = response.json()
			content = response.content
			if not response.ok:
				response.failure("Got wrong response: [" + content + "]")
			else:
				response.success()
				print "Removing " + str(len(resp_json)) + " existing workspaces."
				for wkspc in resp_json:
					id = wkspc["id"]
					if wkspc["status"] != "STOPPED":
						self.stopWorkspace(id)
						self.waitForWorkspaceToStop(id)
					self.deleteWorkspace(id)
		except ValueError:
			response.failure("Got wrong response: [" + content + "]")





class TokenUser(HttpLocust):
	host = cheServerUrl
	task_set = TokenBehavior
	min_wait = 1000
	max_wait = 10000
