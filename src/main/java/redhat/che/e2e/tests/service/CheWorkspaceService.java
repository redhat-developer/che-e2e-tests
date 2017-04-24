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

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;
import okhttp3.Response;
import redhat.che.e2e.tests.resource.CheWorkspace;
import redhat.che.e2e.tests.resource.CheWorkspaceLink;
import redhat.che.e2e.tests.resource.CheWorkspaceStatus;
import redhat.che.e2e.tests.rest.RequestType;
import redhat.che.e2e.tests.rest.RestClient;

public class CheWorkspaceService {

	private static final Logger logger = Logger.getLogger(CheWorkspaceService.class);

	// Interval between querying
	private static long SLEEP_TIME_TICK = 2000;
	// Wait time in seconds
	private static int WAIT_TIME = 300;
	
	public static Object getDocumentFromResponse(Response response) {
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

	public static CheWorkspaceLink getWorkspaceURLFromDocument(Object document) {
		return new CheWorkspaceLink(getWorkspaceIDEURL(document));
	}

	/**
	 * Sent a delete request and wait while workspace is existing.
	 * 
	 * @param workspace
	 *            workspace to delete
	 */
	public static void deleteWorkspace(CheWorkspace workspace) {
		logger.info("Deleting " + workspace);
		RestClient client = new RestClient(workspace.getWorkspaceURL());
		client.sentRequest(null, RequestType.DELETE).close();

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
		client.close();
	}

	private static boolean workspaceExists(RestClient client, CheWorkspace workspace) {
		Response response = client.sentRequest(workspace.getWorkspaceURL(), RequestType.GET);
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
	 * @param workspace
	 *            workspace to stop
	 */
	public static void stopWorkspace(CheWorkspace workspace) {
		logger.info("Stopping " + workspace);
		operateWorkspaceState(workspace, RequestType.DELETE, CheWorkspaceStatus.STOPPED.getStatus());
	}

	/**
	 * Gets current status of a workspace.
	 * 
	 * @param workspace
	 *            workspace to get its status
	 * @return status of workspace
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

	public static String getWorkspaceRuntimeURL(CheWorkspaceLink workspaceIDELink, Object jsonDocument) {
	    String workspacePath = "$[?(@.links[?(@.href=='" + workspaceIDELink.getURL() + "')])]";
	    String linkPath = "$..links[?(@.rel=='start workspace')].href";
	    List<String> wsLinks= JsonPath.read(jsonDocument, workspacePath);
	    JSONArray jsonArray = (JSONArray) JsonPath.read(wsLinks.toString(), linkPath);
	    return jsonArray.get(0).toString();
	}
	
	public static String getWorkspaceURL(CheWorkspaceLink workspaceIDELink, Object jsonDocument) {
	    String workspacePath = "$[?(@.links[?(@.href=='" + workspaceIDELink.getURL() + "')])]";
        String linkPath = "$..links[?(@.rel=='self link')].href";
        List<String> wsLinks= JsonPath.read(jsonDocument, workspacePath);
        JSONArray jsonArray = (JSONArray) JsonPath.read(wsLinks.toString(), linkPath);
        return jsonArray.get(0).toString();
	}
	
	private static String getWorkspaceIDEURL(Object jsonDocument) {
		return JsonPath.read(jsonDocument, "$.href");
	}

	private static String getWorkspaceStatus(Object jsonDocument) {
		return JsonPath.read(jsonDocument, "$.status");
	}
}
