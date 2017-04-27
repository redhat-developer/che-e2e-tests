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
package redhat.che.e2e.tests.provider;

import okhttp3.Response;
import redhat.che.e2e.tests.Utils;
import redhat.che.e2e.tests.resource.CheWorkspace;
import redhat.che.e2e.tests.rest.QueryParam;
import redhat.che.e2e.tests.rest.RequestType;
import redhat.che.e2e.tests.rest.RestClient;
import redhat.che.e2e.tests.service.CheWorkspaceService;

public class CheWorkspaceProvider {
	
	/**
	 * Creates a new workspace via che-starter
	 * 
	 * @param cheStarterURL URL of Che starter
	 * @param openShiftMasterURL URL of OpenShift server where Che server is running in users namespace
	 * @param openShiftToken OpenShift auth token
	 * @param pathToJson  path to a json file containing workspace definition for REST call of Che starter
	 * @return Che workspace
	 */
	public static CheWorkspace createCheWorkspace(String cheStarterURL, String openShiftMasterURL, String keycloakToken,
	            String pathToJson, String namespace) {
	    
			String json = Utils.getTextFromFile(pathToJson);
			json = json.replaceAll("\\{ws.id\\}", "workspace" + System.currentTimeMillis());
			RestClient client = new RestClient(cheStarterURL);
			Response response = client.sentRequest("/workspace/oso", RequestType.POST, json, keycloakToken,
					new QueryParam("masterUrl", openShiftMasterURL), new QueryParam("namespace", namespace));
			Object jsonDocument = CheWorkspaceService.getDocumentFromResponse(response);
			response.close();
			client.close();
			return CheWorkspaceService.getWorkspaceFromDocument(jsonDocument);
	}
}
