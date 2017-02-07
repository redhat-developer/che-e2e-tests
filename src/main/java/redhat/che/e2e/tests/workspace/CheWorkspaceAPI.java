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

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import okhttp3.Response;
import redhat.che.e2e.tests.Utils;
import redhat.che.e2e.tests.rest.CheRestClient;
import redhat.che.e2e.tests.rest.RequestType;

public class CheWorkspaceAPI {

	public static final String RUNNING_STATUS = "RUNNING";
	public static final String STOPPED_STATUS = "STOPPED";

	private static String CREATE_WS_API = "/api/workspace";
	private static String OPERATE_WS_API = "/api/workspace/{id}/runtime";
	private static String WS_API = "/api/workspace/{id}";

	private static long SLEEP_TIME_TICK = 2000;
	// Wait time in seconds
	private static int WAIT_TIME = 300;

	public static String getCreateWorkspaceAPI() {
		return CREATE_WS_API;
	}

	/**
	 * Gets Workspace API endpoint for operating workspace runtime.
	 * 
	 * @param id
	 * @return
	 */
	public static String getWorkspaceRuntimeAPI(String id) {
		return OPERATE_WS_API.replace("{id}", id);
	}

	public static String getWorkspaceAPI(String id) {
		return WS_API.replace("{id}", id);
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
		CheRestClient client = new CheRestClient(cheServerURL);
		String requestBody = Utils.getTextFromFile(pathToJSON);
		Response response = client.sentRequest(getCreateWorkspaceAPI(), RequestType.POST, requestBody);
		Object document = getDocumentFromResponse(response);
		return new CheWorkspace(getWorkspaceId(document), getWorkspaceName(document), getWorkspaceURL(document));
	}

	/**
	 * Sent a delete request and wait while workspace is existing.
	 * 
	 * @param cheServerURL
	 * @param id
	 */
	public static void deleteWorkspace(String cheServerURL, String id) {
		CheRestClient client = new CheRestClient(cheServerURL);
		client.sentRequest(getWorkspaceAPI(id), RequestType.DELETE);
		
		int counter = 0;
		int maxCount = Math.round(WAIT_TIME / (SLEEP_TIME_TICK / 1000));
		while (counter < maxCount && workspaceExists(cheServerURL, id)) {
			counter++;
			try {
				Thread.sleep(SLEEP_TIME_TICK);
			} catch (InterruptedException e) {
			}
		}

		if (counter == maxCount && workspaceExists(cheServerURL, id)) {
			throw new RuntimeException("After waiting for " + WAIT_TIME + " seconds the workspace is still"
					+ " existing");
		}
	}
	
	private static boolean workspaceExists(String cheServerURL, String id) {
		CheRestClient client = new CheRestClient(cheServerURL);
		return client.sentRequest(getWorkspaceAPI(id), RequestType.GET).isSuccessful();
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
	 * @param URL
	 * @param id
	 */
	public static void startWorkspace(String URL, String id) {
		operateWorkspaceState(URL, id, RequestType.POST, RUNNING_STATUS);
	}

	/**
	 * Stops a workspace and wait until it is stopped.
	 * @param URL
	 * @param id
	 */
	public static void stopWorkspace(String URL, String id) {
		operateWorkspaceState(URL, id, RequestType.DELETE, STOPPED_STATUS);
	}

	/**
	 * Gets current status of a workspace.
	 * 
	 * @param URL
	 * @param id
	 * @return
	 */
	public static String getWorkspaceStatus(String URL, String id) {
		CheRestClient client = new CheRestClient(URL);
		return getWorkspaceStatus(getDocumentFromResponse(client.sentRequest(getWorkspaceAPI(id), RequestType.GET)));
	}

	private static void operateWorkspaceState(String URL, String id, RequestType requestType, String resultState) {
		CheRestClient client = new CheRestClient(URL);
		client.sentRequest(getWorkspaceRuntimeAPI(id), requestType);
		int counter = 0;
		int maxCount = Math.round(WAIT_TIME / (SLEEP_TIME_TICK / 1000));
		while (counter < maxCount && !resultState.equals(getWorkspaceStatus(URL, id))) {
			counter++;
			try {
				Thread.sleep(SLEEP_TIME_TICK);
			} catch (InterruptedException e) {
			}
		}

		if (counter == maxCount && resultState.equals(getWorkspaceStatus(URL, id))) {
			throw new RuntimeException("After waiting for " + WAIT_TIME + " seconds the workspace is still"
					+ " not in state " + resultState);
		}
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
