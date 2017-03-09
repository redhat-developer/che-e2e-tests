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
package redhat.che.e2e.tests.factory;

import org.apache.log4j.Logger;

import okhttp3.Response;
import redhat.che.e2e.tests.ObjectState;
import redhat.che.e2e.tests.Utils;
import redhat.che.e2e.tests.resource.CheWorkspace;
import redhat.che.e2e.tests.rest.QueryParam;
import redhat.che.e2e.tests.rest.RequestType;
import redhat.che.e2e.tests.rest.RestClient;
import redhat.che.e2e.tests.service.CheWorkspaceService;

public class CheWorkspaceFactory {
	
	private static final Logger logger = Logger.getLogger(CheWorkspaceFactory.class);
	
	/**
	 * Gets a Che workspace.<br><br>
	 * To create a new workspace via che-starter, pass folowing arguments:<br>
	 * &lt;String cheStarterURL&gt; URL of Che starter<br>
	 * &lt;String openShiftMasterURL&gt; URL of OpenShift server where Che server is running in users namespace<br>
	 * &lt;String openShiftToken&gt; user's authorization token<br>
	 * &lt;String pathToWorkspaceJSON&gt; path to a json file containing workspace definition for REST call of Che starter<br>
	 * <br><br>
	 * To get a new Che workspace via CUSTOM way (direct calls of Che API), pass following arguments:<br>
	 * &lt;String cheServerURL&gt; URL of Che server to create workspace on<br>
	 * &lt;String pathToWorkspaceJSON&gt; path to a json file containing workspace definition for REST call to Che server
	 * @param state
	 * @param args
	 * @return Che workspace
	 */
	public static CheWorkspace getCheWorkspace(ObjectState state, String... args) {
		if (ObjectState.CUSTOM.equals(state)) {
			logger.info("Creating a new Che workspace directly via Che server REST API");
			if (args.length != 2) {
				throw new RuntimeException("Insufficient amount of arguments. Pass Che server URL and path to"
						+ " JSON file with workspace configuration");
			}
			String cheServerURL = args[0];
			String pathToJson = args[1];
			CheWorkspace workspace = CheWorkspaceService.createWorkspace(cheServerURL, pathToJson);
			CheWorkspaceService.startWorkspace(workspace);
			return workspace;
		} else if (ObjectState.NEW.equals(state)) {
			if (args.length != 4) {
				throw new RuntimeException("Incorrect amount of arguments. Pass OpenShift master URL,"
						+ " token and JSON of request to create a new workspace");
				
			}
			String cheStarterURL = args[0];
			String openShiftMasterURL = args[1];
			String openShiftToken = args[2];
			String json = Utils.getTextFromFile(args[3]);
			RestClient client = new RestClient(cheStarterURL);
			Response response = client.sentRequest("/workspace", RequestType.POST, json, openShiftToken,
					new QueryParam("masterUrl", openShiftMasterURL));
			return CheWorkspaceService.getWorkspaceFromDocument(CheWorkspaceService.getDocumentFromResponse(response));
		}
		throw new IllegalArgumentException("Unsupported Object state");
	}
}
