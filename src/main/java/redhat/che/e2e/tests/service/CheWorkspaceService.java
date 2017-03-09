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
package redhat.che.e2e.tests.service;

import java.util.List;

import org.apache.log4j.Logger;

import com.jayway.jsonpath.JsonPath;

import okhttp3.Response;
import redhat.che.e2e.tests.Utils;
import redhat.che.e2e.tests.resource.CheWorkspace;
import redhat.che.e2e.tests.resource.CheWorkspaceStatus;
import redhat.che.e2e.tests.rest.RestClient;
import redhat.che.e2e.tests.rest.RequestType;

public class CheWorkspaceService extends AbstractService {

	private static final Logger logger = Logger.getLogger(CheWorkspaceService.class);

	private static String CREATE_WORKSPACE_ENDPOINT = "/api/workspace";
	private static String WORKSPACE_ENDPOINT = "/api/workspace/{id}";

	private static String WS_NAME_VAR = "\\{workspace.name\\}";

	private static String getCreateWorkspaceEndpoint() {
		return CREATE_WORKSPACE_ENDPOINT;
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
		RestClient client = new RestClient(cheServerURL);
		String requestBody = Utils.getTextFromFile(pathToJSON).replaceAll(WS_NAME_VAR, getWorkspaceName());
		Response response = client.sentRequest(getCreateWorkspaceEndpoint(), RequestType.POST, requestBody);
		Object document = getDocumentFromResponse(response);
		response.close();
		CheWorkspace workspace = getWorkspaceFromDocument(document);
		workspace.setServerURL(cheServerURL);
		return workspace;
	}

	public static CheWorkspace getWorkspaceFromDocument(Object document) {
		return new CheWorkspace(getWorkspaceId(document), getWorkspaceName(document), getWorkspaceIDEURL(document),
				getWorkspaceURL(document), getWorkspaceRuntime(document));
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
		RestClient client = new RestClient(workspace.getServerURL());
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
			throw new RuntimeException(
					"After waiting for " + WAIT_TIME + " seconds the workspace is still" + " existing");
		} else {
			logger.info("Workspace has been successfully deleted from Che server");
		}
	}

	private static boolean workspaceExists(RestClient client, CheWorkspace workspace) {
		Response response = client.sentRequest(getWorkspaceEndpoint(workspace.getId()), RequestType.GET);
		boolean isSuccessful = response.isSuccessful();
		response.close();
		return isSuccessful;
	}

	/**
	 * Starts a workspace and wait until it is started.
	 * 
	 * @param workspace
	 *            workspace to start
	 */
	public static void startWorkspace(CheWorkspace workspace) {
		logger.info("Starting " + workspace);
		operateWorkspaceState(workspace, RequestType.POST, CheWorkspaceStatus.RUNNING.getStatus());
	}

	/**
	 * Stops a workspace and wait until it is stopped.
	 * 
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
		logger.info("Getting status of " + workspace);
		RestClient client = new RestClient(workspace.getWorkspaceURL());
		String status = getWorkspaceStatus(client, workspace);
		client.close();
		return status;

	}

	private static String getWorkspaceStatus(RestClient client, CheWorkspace workspace) {
		Response response = client.sentRequest(null, RequestType.GET);
		Object document = getDocumentFromResponse(response);
		response.close();
		return getWorkspaceStatus(document);
	}

	private static void operateWorkspaceState(CheWorkspace workspace, RequestType requestType, String resultState) {
		RestClient client = new RestClient(workspace.getWorkspaceRuntimeURL());
		client.sentRequest(null, requestType).close();
		client.close();

		waitUntilWorkspaceGetsToState(workspace, resultState);

		String currentState = getWorkspaceStatus(workspace);
		if (CheWorkspaceStatus.RUNNING.getStatus().equals(currentState)) {
			logger.info("Settings WS agent URL for workspace");
			client = new RestClient(workspace.getWorkspaceURL());
			Response response = client.sentRequest(null, RequestType.GET);
			Object document = getDocumentFromResponse(response);
			response.close();
			workspace.setWsAgentURL(getWsAgentURL(document));
		} else if (CheWorkspaceStatus.STOPPED.getStatus().equals(currentState)) {
			workspace.setWsAgentURL(null);
		}
	}

	public static void waitUntilWorkspaceGetsToState(CheWorkspace workspace, String resultState) {
		RestClient client = new RestClient(workspace.getWorkspaceURL());
		int counter = 0;
		int maxCount = Math.round(WAIT_TIME / (SLEEP_TIME_TICK / 1000));
		String currentState = getWorkspaceStatus(client, workspace);
		logger.info("Waiting for " + WAIT_TIME + " seconds until workspace gets from state " + currentState
				+ " to state " + resultState);
		while (counter < maxCount && !resultState.equals(currentState)) {
			counter++;
			try {
				Thread.sleep(SLEEP_TIME_TICK);
			} catch (InterruptedException e) {
			}
			currentState = getWorkspaceStatus(client, workspace);
		}

		if (counter == maxCount && !resultState.equals(currentState)) {
			logger.error("Workspace has not successfuly changed its state in required time period of" + WAIT_TIME
					+ " seconds");
			throw new RuntimeException("After waiting for " + WAIT_TIME + " seconds the workspace is still"
					+ " not in state " + resultState);
		}
	}

	private static String getWorkspaceIDEURL(Object jsonDocument) {
		List<String> wsLinks = JsonPath.read(jsonDocument, "$.links[?(@.rel=='ide url')].href");
		return wsLinks.get(0);
	}

	private static String getWorkspaceURL(Object jsonDocument) {
		List<String> wsLinks = JsonPath.read(jsonDocument, "$.links[?(@.rel=='self link')].href");
		return wsLinks.get(0);
	}

	private static String getWorkspaceRuntime(Object jsonDocument) {
		List<String> wsLinks = JsonPath.read(jsonDocument, "$.links[?(@.rel=='start workspace')].href");
		return wsLinks.get(0);
	}

	private static String getWsAgentURL(Object jsonDocument) {
		return JsonPath.read(jsonDocument, "$.runtime.devMachine.runtime.servers.4401/tcp.url");
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
