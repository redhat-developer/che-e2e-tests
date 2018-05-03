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

import com.redhat.arquillian.che.config.CheExtensionConfiguration;
import com.redhat.arquillian.che.resource.CheWorkspace;
import com.redhat.arquillian.che.rest.QueryParam;
import com.redhat.arquillian.che.rest.RequestType;
import com.redhat.arquillian.che.rest.RestClient;
import com.redhat.arquillian.che.service.CheWorkspaceService;
import com.redhat.arquillian.che.util.Constants;
import com.redhat.arquillian.che.util.Utils;
import okhttp3.Response;
import org.apache.log4j.Logger;

import java.io.InputStream;

import static com.redhat.arquillian.che.util.Constants.CREATE_WORKSPACE_REQUEST_VERTX_JSON;

public class CheWorkspaceProvider {
	
	private static final Logger LOG = Logger.getLogger(CheWorkspaceProvider.class);

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
        openShiftMasterURL = config.getCustomCheServerFullURL().isEmpty()
                ? config.getOpenshiftMasterUrl()
                : config.getCustomCheServerFullURL();
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
        RestClient cheStarterClient = new RestClient(cheStarterURL);
        Response response = cheStarterClient.sendRequest(
                path,
                RequestType.POST,
                json,
                keycloakToken,
                new QueryParam("masterUrl", openShiftMasterURL),
                new QueryParam("namespace", namespace)
        );
        Object jsonDocument = CheWorkspaceService.getDocumentFromResponse(response);
        response.close();
        cheStarterClient.close();
        CheWorkspace workspace = CheWorkspaceService.getWorkspaceFromDocument(jsonDocument);
        startWorkspace(workspace);
        return workspace;
    }

    public CheWorkspace getCreatedWorkspace() {
        String path = "/workspace";
        RestClient cheStarterClient = new RestClient(cheStarterURL);

        Response response = cheStarterClient.sendRequest(
                path,
                RequestType.GET,
                null,
                keycloakToken,
                new QueryParam("masterUrl", openShiftMasterURL),
                new QueryParam("namespace", namespace)
        );
        Object jsonDocument = CheWorkspaceService.getDocumentFromResponse(response);
        response.close();
        cheStarterClient.close();
        CheWorkspace workspace = CheWorkspaceService.getWorkspaceFromDocument(jsonDocument);
        startWorkspace(workspace);
        return workspace;
    }

    public static CheExtensionConfiguration getConfiguration() {
        return configuration;
    }

    private void startWorkspace(CheWorkspace workspace) {
        Response response;
        LOG.info("Workspace self link:" + workspace.getSelfLink());
        RestClient cheServerClient = new RestClient(workspace.getSelfLink());
        response = cheServerClient.sendRequest(
                "/runtime",
                RequestType.POST,
                null,
                keycloakToken
        );
        if (!response.isSuccessful()) {
            throw new IllegalStateException("Che server failed to start workspace:" + response.message());
        } else {
            LOG.info("Start request sent successfully:" + response.message());
        }
        cheServerClient.close();
    }

}
