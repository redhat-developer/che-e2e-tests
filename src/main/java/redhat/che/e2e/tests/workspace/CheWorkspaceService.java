/******************************************************************************* 
 * Copyright (c) 2017 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
*/
package redhat.che.e2e.tests.workspace;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import okhttp3.Response;
import redhat.che.e2e.tests.Utils;
import redhat.che.e2e.tests.rest.CheRestClient;
import redhat.che.e2e.tests.rest.RequestType;

public class CheWorkspaceService {

	private static final Logger logger = Logger.getLogger(CheWorkspaceService.class);
	
	private static String CREATE_WORKSPACE_ENDPOINT = "/api/workspace";
	private static String OPERATE_WORKSPACE_ENDPOINT = "/api/workspace/{id}/runtime";
	private static String WORKSPACE_ENDPOINT = "/api/workspace/{id}";
	
	private static String CREATE_PROJECT_WSAGENT_ENDPOINT = "/ext/project";
	
	private static String WS_NAME_VAR = "\\{workspace.name\\}";
	
	// Interval between querying
	private static long SLEEP_TIME_TICK = 2000;
	// Wait time in seconds
	private static int WAIT_TIME = 300;

	private static String getCreateWorkspaceEndpoint() {
		return CREATE_WORKSPACE_ENDPOINT;
	}

	/**
	 * Gets Workspace API endpoint for operating workspace runtime.
	 * 
	 * @param id
	 * @return
	 */
	private static String getWorkspaceRuntimeEndpoint(String id) {
		return OPERATE_WORKSPACE_ENDPOINT.replace("{id}", id);
	}

	private static String getWorkspaceEndpoint(String id) {
		return WORKSPACE_ENDPOINT.replace("{id}", id);
	}

	/**
	 * Create a new Che workspace on a Che server. Workspace is 
	 * 
	 * @param cheServerURL
	 *            URL of Che server
	 * @param pathToJSON
	 *            path to workspace JSON request
	 * @return CheWorkspace created on a Che server from provided JSON request
	 */
	public static CheWorkspace createWorkspace(String cheServerURL, String pathToJSON) {
		logger.info("Creating a new Che workspace on server " + cheServerURL);
		CheRestClient client = new CheRestClient(cheServerURL);
		String requestBody = Utils.getTextFromFile(pathToJSON).replaceAll(WS_NAME_VAR, getWorkspaceName());
		Response response = client.sentRequest(getCreateWorkspaceEndpoint(), RequestType.POST, requestBody);
		Object document = getDocumentFromResponse(response);
		response.close();
		return new CheWorkspace(getWorkspaceId(document), getWorkspaceName(document), getWorkspaceURL(document),
				cheServerURL);
	}
	
	private static String getWorkspaceName() {
		return "ws" + Long.valueOf(System.currentTimeMillis()).hashCode();
	}
	
	/**
	 * Sent a delete request and wait while workspace is existing.
	 * 
	 * @param cheServerURL
	 * @param id
	 */
	public static void deleteWorkspace(CheWorkspace workspace) {
		logger.info("Deleting " + workspace);
		CheRestClient client = new CheRestClient(workspace.getServerURL());
		client.sentRequest(getWorkspaceEndpoint(workspace.getId()), RequestType.DELETE).close();
		
		int counter = 0;
		int maxCount = Math.round(WAIT_TIME / (SLEEP_TIME_TICK / 1000));
		logger.info("Waiting for " + WAIT_TIME + " seconds until workspace is deleted from Che server.");
		while (counter < maxCount && workspaceExists(client, workspace)) {
			counter++;
			try {
				Thread.sleep(SLEEP_TIME_TICK);
			} catch (InterruptedException e) {
			}
		}

		if (counter == maxCount && workspaceExists(client, workspace)) {
			logger.error("Workspace has not been deleted on a server after waiting for " + WAIT_TIME + " seconds");
			throw new RuntimeException("After waiting for " + WAIT_TIME + " seconds the workspace is still"
					+ " existing");
		} else {
			logger.info("Workspace has been successfully deleted from Che server");
		}
	}
	
	private static boolean workspaceExists(CheRestClient client, CheWorkspace workspace) {
		Response response = client.sentRequest(getWorkspaceEndpoint(workspace.getId()), RequestType.GET);
		boolean isSuccessful = response.isSuccessful();
		response.close();
		return isSuccessful;
	}
	
	private static Object getDocumentFromResponse(Response response) {
		String responseString = null;
		if (response.isSuccessful()) {
			try {
				responseString = response.body().string();
			} catch (IOException e) {
			}
		}
		if (responseString == null) {
			throw new RuntimeException("Something went wrong and response is empty");
		}
		return Configuration.defaultConfiguration().jsonProvider().parse(responseString);
	}

	/**
	 * Starts a workspace and wait until it is started.
	 * 
	 * @param workspace workspace to start
	 */
	public static void startWorkspace(CheWorkspace workspace) {
		logger.info("Starting " + workspace);
		operateWorkspaceState(workspace, RequestType.POST, CheWorkspaceStatus.RUNNING.getStatus());
	}
	
	/**
	 * Stops a workspace and wait until it is stopped.
	 * @param URL
	 * @param id
	 */
	public static void stopWorkspace(CheWorkspace workspace) {
		logger.info("Stopping " + workspace);
		operateWorkspaceState(workspace, RequestType.DELETE, CheWorkspaceStatus.STOPPED.getStatus());
	}

	/**
	 * Gets current status of a workspace.
	 * 
	 * @param URL
	 * @param id
	 * @return
	 */
	public static String getWorkspaceStatus(CheWorkspace workspace) {
		logger.info("Getting status of " +workspace);
		CheRestClient client = new CheRestClient(workspace.getServerURL());
		return getWorkspaceStatus(client, workspace);
	}
	
	private static String getWorkspaceStatus(CheRestClient client, CheWorkspace workspace) {
		Response response = client.sentRequest(getWorkspaceEndpoint(workspace.getId()), RequestType.GET);
		Object document = getDocumentFromResponse(response);
		response.close();
		return getWorkspaceStatus(document);
	}

	private static void operateWorkspaceState(CheWorkspace workspace, RequestType requestType, String resultState) {
		CheRestClient client = new CheRestClient(workspace.getServerURL());
		client.sentRequest(getWorkspaceRuntimeEndpoint(workspace.getId()), requestType).close();
		int counter = 0;
		int maxCount = Math.round(WAIT_TIME / (SLEEP_TIME_TICK / 1000));
		String currentState = getWorkspaceStatus(client, workspace);
		logger.info("Waiting for " + WAIT_TIME + " seconds until workspace gets from state "
				+ currentState + " to state " + resultState);
		while (counter < maxCount && !resultState.equals(currentState)) {
			counter++;
			try {
				Thread.sleep(SLEEP_TIME_TICK);
			} catch (InterruptedException e) {
			}
			currentState = getWorkspaceStatus(client, workspace);
		}
		
		if (counter == maxCount && !resultState.equals(currentState)) {
			logger.error("Workspace has not successfuly changed its state in required time period of"
					+ WAIT_TIME + " seconds");
			throw new RuntimeException("After waiting for " + WAIT_TIME + " seconds the workspace is still"
					+ " not in state " + resultState);
		}
		
		if (CheWorkspaceStatus.RUNNING.getStatus().equals(currentState)) {
			logger.info("Settings WS agent URL for workspace");
			Response response = client.sentRequest(getWorkspaceEndpoint(workspace.getId()), RequestType.GET);
			Object document = getDocumentFromResponse(response);
			response.close();
			workspace.setWsAgentURL(getWsAgentURL(document));
		} else if (CheWorkspaceStatus.STOPPED.getStatus().equals(currentState)) {
			workspace.setWsAgentURL(null);
		}
	}

	private static String getWsAgentURL(Object jsonDocument) {
		return JsonPath.read(jsonDocument, "$.runtime.devMachine.runtime.servers.4401/tcp.url");
	}
	
	private static String getWorkspaceURL(Object jsonDocument) {
		List<String> wsLinks = JsonPath.read(jsonDocument, "$.links[?(@.rel=='ide url')].href");
		return wsLinks.get(0);
	}

	private static String getWorkspaceId(Object jsonDocument) {
		return JsonPath.read(jsonDocument, "$.id");
	}

	private static String getWorkspaceName(Object jsonDocument) {
		return JsonPath.read(jsonDocument, "$.config.name");
	}

	private static String getWorkspaceStatus(Object jsonDocument) {
		return JsonPath.read(jsonDocument, "$.status");
	}
}
