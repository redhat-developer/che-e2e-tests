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
package com.redhat.arquillian.che.provider;

import static com.redhat.arquillian.che.util.Constants.CREATE_WORKSPACE_REQUEST_VERTX_JSON;

import java.io.InputStream;
import com.redhat.arquillian.che.resource.StackService;
import org.apache.log4j.Logger;
import com.redhat.arquillian.che.config.CheExtensionConfiguration;
import com.redhat.arquillian.che.resource.CheWorkspaceStatus;
import com.redhat.arquillian.che.util.Constants;
import com.redhat.arquillian.che.util.Utils;
import com.redhat.arquillian.che.resource.CheWorkspace;
import com.redhat.arquillian.che.rest.QueryParam;
import com.redhat.arquillian.che.rest.RequestType;
import com.redhat.arquillian.che.rest.RestClient;
import com.redhat.arquillian.che.service.CheWorkspaceService;
import okhttp3.Response;

public class CheWorkspaceProvider {
	
	private static final Logger logger = Logger.getLogger(CheWorkspaceProvider.class);

    private static String cheStarterURL;
    private static String openShiftMasterURL;
    private static String openshiftToken;
    private static String keycloakToken;
    private static String namespace;
    private static String cheWorkspaceName;

    private static CheExtensionConfiguration configuration;

    public CheWorkspaceProvider(CheExtensionConfiguration config){
        configuration = config;
        cheStarterURL = config.getCheStarterUrl();
        openShiftMasterURL = config.getOpenshiftMasterUrl();
        openshiftToken = config.getOpenshiftToken();
        keycloakToken = config.getKeycloakToken();
        namespace = config.getOpenshiftNamespace();
        cheWorkspaceName = config.getCheWorkspaceName();
    }

    /**
     * Creates a new workspace via che-starter
     * 
     * @param pathToJson
     *            path to json of workspace create params
     */
    public CheWorkspace createCheWorkspaceOSO(String pathToJson) {

        return createWorkspace(pathToJson, "/workspace/oso");
    }

    /**
     * Creates Che workspace on a server in users namespace on OpenShift. Uses
     * keycloak endpoint
     *
     * @param pathToJson
     *            path to json of workspace create params
     * @return Che workspace
     */
    public CheWorkspace createCheWorkspace(String pathToJson) {
        
        return createWorkspace(pathToJson, "/workspace");
    }
    
	private CheWorkspace createWorkspace(String pathToJson, String path) {

        String json;
        if (pathToJson == null) {
            InputStream jsonStream = Constants.class.getClassLoader().getResourceAsStream(CREATE_WORKSPACE_REQUEST_VERTX_JSON);
            json = Utils.getTextFromFile(jsonStream);
        }else {
            InputStream jsonStream = Constants.class.getClassLoader().getResourceAsStream(pathToJson);
            json = Utils.getTextFromFile(jsonStream);
        }
        RestClient client = new RestClient(cheStarterURL);
        Response response = client.sentRequest(path, RequestType.POST, json, keycloakToken,
                new QueryParam("masterUrl", openShiftMasterURL), new QueryParam("namespace", namespace));
        Object jsonDocument = CheWorkspaceService.getDocumentFromResponse(response);
        response.close();
        client.close();
        return CheWorkspaceService.getWorkspaceFromDocument(jsonDocument);
    }

    public CheWorkspace getCreatedWorkspace() {
        String path = "/workspace";
        RestClient client = new RestClient(cheStarterURL);

        Response response = client.sentRequest(path, RequestType.GET, null, keycloakToken,
                new QueryParam("masterUrl", openShiftMasterURL), new QueryParam("namespace", namespace));
        Object jsonDocument = CheWorkspaceService.getDocumentFromResponse(response);
        response.close();
        client.close();
        return CheWorkspaceService.getWorkspaceFromDocument(jsonDocument, cheWorkspaceName);
    }

    public static CheExtensionConfiguration getConfiguration() {
        return configuration;
    }
}
