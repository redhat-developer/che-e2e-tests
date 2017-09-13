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
import com.redhat.arquillian.che.resource.CheWorkspaceStatus;
import com.redhat.arquillian.che.util.Constants;
import com.redhat.arquillian.che.util.Utils;
import com.redhat.arquillian.che.resource.CheWorkspace;
import com.redhat.arquillian.che.rest.QueryParam;
import com.redhat.arquillian.che.rest.RequestType;
import com.redhat.arquillian.che.rest.RestClient;
import com.redhat.arquillian.che.service.CheWorkspaceService;
import java.io.InputStream;
import okhttp3.Response;

import static com.redhat.arquillian.che.util.Constants.CREATE_WORKSPACE_REQUEST_JSON;

public class CheWorkspaceProvider {

    private static String cheStarterURL;
    private static String openShiftMasterURL;
    private static String openshiftToken;
    private static  String keycloakToken;
    private static String namespace;

    public CheWorkspaceProvider(CheExtensionConfiguration config){
        cheStarterURL = config.getCheStarterUrl();
        openShiftMasterURL = config.getOpenshiftMasterUrl();
        openshiftToken = config.getOpenshiftToken();
        keycloakToken = config.getKeycloakToken();
        namespace = config.getOpenshiftNamespace();
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
            InputStream jsonStream = Constants.class.getClassLoader().getResourceAsStream(CREATE_WORKSPACE_REQUEST_JSON);
            json = Utils.getTextFromFile(jsonStream);
        }else {
            json = Utils.getTextFromFile(pathToJson);
        }
        RestClient client = new RestClient(cheStarterURL);
        Response response = client.sentRequest(path, RequestType.POST, json, keycloakToken,
                new QueryParam("masterUrl", openShiftMasterURL), new QueryParam("namespace", namespace));
        Object jsonDocument = CheWorkspaceService.getDocumentFromResponse(response);
        response.close();
        client.close();
        return CheWorkspaceService.getWorkspaceFromDocument(jsonDocument);
    }

    public boolean stopWorkspace(CheWorkspace workspace){
        CheWorkspaceService.stopWorkspace(workspace, keycloakToken);
        if(CheWorkspaceService.getWorkspaceStatus(workspace, keycloakToken).equals(CheWorkspaceStatus.STOPPED.getStatus())){
            return true;
        }
        return  false;
    }

    public boolean startWorkspace(CheWorkspace workspace){
        CheWorkspaceService.startWorkspace(workspace, keycloakToken);
        if(CheWorkspaceService.getWorkspaceStatus(workspace, keycloakToken).equals(CheWorkspaceStatus.RUNNING.getStatus())){
            return true;
        }
        return  false;
    }
}
