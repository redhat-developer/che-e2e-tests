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
package com.redhat.arquillian.che.service;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.redhat.arquillian.che.resource.CheWorkspace;
import com.redhat.arquillian.che.resource.CheWorkspaceStatus;
import com.redhat.arquillian.che.rest.RequestType;
import com.redhat.arquillian.che.rest.RestClient;
import com.redhat.arquillian.che.util.OpenShiftHelper;

import okhttp3.Response;

public class CheWorkspaceService {

	private static final Logger logger = Logger.getLogger(CheWorkspaceService.class);

	// Interval between querying
	private static long SLEEP_TIME_TICK = 2000;
	// Wait time in seconds
	private static int WAIT_TIME = 300;
	
	public static Object getDocumentFromResponse(Response response) {
		if (response == null) {
			logger.error(OpenShiftHelper.getCheLogs());
			throw new NullPointerException("Response was null");
		}
		String responseString = null;
		if (response.isSuccessful()) {
			try {
				responseString = response.body().string();
			} catch (IOException e) {
			}
		}
		if (responseString == null) {
			throw new RuntimeException(
				"Something went wrong and response is empty. The message contains: " + response.message());
		}
		return Configuration.defaultConfiguration().jsonProvider().parse(responseString);
	}

	public static CheWorkspace getWorkspaceFromDocument(Object jsonDocument) {
		return new CheWorkspace(getWorkspaceIDELink(jsonDocument), getWorkspaceSelfLink(jsonDocument),
		        getWorkspaceRuntimeLink(jsonDocument));
	}

	/**
	 * Sent a delete request and wait while workspace is existing.
	 * 
	 * @param workspace
	 *            workspace to delete
	 */
	public static void deleteWorkspace(CheWorkspace workspace, String token) {
		logger.info("Deleting " + workspace);
		RestClient client = new RestClient(workspace.getSelfLink());
		client.sentRequest(null, RequestType.DELETE, null, token).close();

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
		Response response = client.sentRequest(workspace.getSelfLink(), RequestType.GET);
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
	public static void startWorkspace(CheWorkspace workspace, String authorizationToken) {
		logger.info("Starting " + workspace);
		operateWorkspaceState(workspace, RequestType.POST, CheWorkspaceStatus.RUNNING.getStatus(), authorizationToken);
	}

	/**
	 * Stops a workspace and wait until it is stopped.
	 * 
	 * @param workspace
	 *            workspace to stop
	 */
	public static void stopWorkspace(CheWorkspace workspace, String authorizationToken) {
		operateWorkspaceState(workspace, RequestType.DELETE, CheWorkspaceStatus.STOPPED.getStatus(), authorizationToken);
	}

	/**
	 * Gets current status of a workspace.
	 * 
	 * @param workspace
	 *            workspace to get its status
	 * @return status of workspace
	 */
	public static String getWorkspaceStatus(CheWorkspace workspace, String authorizationToken) {
		logger.info("Getting status of " + workspace);
		RestClient client = new RestClient(workspace.getSelfLink());
		String status = getWorkspaceStatus(client, workspace, authorizationToken);
		client.close();
		return status;

	}

	private static String getWorkspaceStatus(RestClient client, CheWorkspace workspace, String authorizationToken) {
		Response response = client.sentRequest(null, RequestType.GET, null, authorizationToken);
		Object document = getDocumentFromResponse(response);
		response.close();
		return getWorkspaceStatus(document);
	}

	private static void operateWorkspaceState(CheWorkspace workspace, RequestType requestType, String resultState,
		String authorizationToken) {
		RestClient client = new RestClient(workspace.getRuntimeLink());
		client.sentRequest(null, requestType, null, authorizationToken).close();
		client.close();
		try {
			waitUntilWorkspaceGetsToState(workspace, resultState, authorizationToken);
		} catch (Throwable throwable){
			logger.error(OpenShiftHelper.getCheLogs());
			throw throwable;
		}
	}

	public static void waitUntilWorkspaceGetsToState(CheWorkspace workspace, String resultState,
		String authorizationToken) {
		RestClient client = new RestClient(workspace.getSelfLink());
		int counter = 0;
		int maxCount = Math.round(WAIT_TIME / (SLEEP_TIME_TICK / 1000));
		String currentState = getWorkspaceStatus(client, workspace, authorizationToken);
		logger.info("Waiting for " + WAIT_TIME + " seconds until workspace gets from state " + currentState
				+ " to state " + resultState);
		while (counter < maxCount && !resultState.equals(currentState)) {
			counter++;
			try {
				Thread.sleep(SLEEP_TIME_TICK);
			} catch (InterruptedException e) {
			}
			currentState = getWorkspaceStatus(client, workspace, authorizationToken);
		}

		if (counter == maxCount && !resultState.equals(currentState)) {
			logger.error("Workspace has not successfuly changed its state in required time period of " + WAIT_TIME
					+ " seconds");
			throw new RuntimeException("After waiting for " + WAIT_TIME + " seconds the workspace is still"
					+ " not in state " + resultState);
		}
	}
	
	public static String getWorkspaceRuntimeLink(Object jsonDocument) {
	    return getLinkHref(jsonDocument, "start workspace");
	}
	
	
	public static String getWorkspaceSelfLink(Object jsonDocument) {
	    return getLinkHref(jsonDocument, "self link");
	}
	
	public static String getWorkspaceIDELink(Object jsonDocument) {
		return getLinkHref(jsonDocument, "ide url");
	}

	private static String getWorkspaceStatus(Object jsonDocument) {
		return JsonPath.read(jsonDocument, "$.status");
	}

    private static String getLinkHref(Object workspaceDocument, String rel) {
        String linkPath = "$..links[?(@.rel=='" + rel + "')].href";
        List<String> wsLinks= JsonPath.read(workspaceDocument, linkPath);
        return wsLinks.get(0).toString();
    }
}
