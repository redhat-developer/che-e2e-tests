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

import org.apache.log4j.Logger;

import okhttp3.Response;
import redhat.che.e2e.tests.Utils;
import redhat.che.e2e.tests.resource.CheWorkspace;
import redhat.che.e2e.tests.rest.CheRestClient;
import redhat.che.e2e.tests.rest.RequestType;

public class ProjectService extends AbstractService {

	private static final Logger logger = Logger.getLogger(ProjectService.class);
	
	private static String CREATE_PROJECT_ENDPOINT = "/project/batch";
	
	private static String getCreateProjectEndpoint() {
		return CREATE_PROJECT_ENDPOINT;
	}
	
	public static void createNewProject(CheWorkspace workspace, String pathToProjectTemplate) {
		logger.info("Creating new project in workspace with ID " + workspace.getId() + " and name " + workspace.getName() +
				" from resource " + pathToProjectTemplate);
		CheRestClient client = new CheRestClient(workspace.getWsAgentURL());
		String requestBody = Utils.getTextFromFile(pathToProjectTemplate);
		Response response = client.sentRequest(getCreateProjectEndpoint(), RequestType.POST, requestBody);
		if (response.isSuccessful()) {
			logger.info("Project successfuly created");
		} else {
			logger.error("Project creation failed");
			throw new RuntimeException("Failed to create a new project in workspace");
		}
		response.close();
	}
	
}

