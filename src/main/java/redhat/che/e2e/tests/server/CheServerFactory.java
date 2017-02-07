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
package redhat.che.e2e.tests.server;

import java.net.HttpURLConnection;
import java.net.URL;

import redhat.che.e2e.tests.ObjectState;

public class CheServerFactory {

	//curl -X GET -H "Authorization: Bearer <token>" https://OPENSHIFT_URL:8443/oapi/v1 
	// --insecure
	
	/**
	 * Gets a Che server.
	 * To get an existing Che server, pass following arguments:
	 * <String cheServerURL>
	 * @param state
	 * @param args
	 * @return Che server
	 */
	public static CheServer getCheServer(ObjectState state, String... args) {
		if (state.equals(ObjectState.EXISTING)) {
			if (args.length != 1) {
				throw new RuntimeException("Insufficient amount of arguments. Pass OpenShift server URL.");
			}
			return getExistingCheServer(args[0]);
		}
		if (state.equals(ObjectState.NEW)) {
			////////////////////////////////////
			// HERE GOES CHE STARTER STUFF/////
			///////////////////////////////////
			throw new UnsupportedOperationException("Creating and getting a new server is not supported yet");
		}
		throw new IllegalArgumentException("Unsupported object state");
	}
	
	private static CheServer getExistingCheServer(String URL) {
		if (isServerReachable(URL)) {
			return new CheServer(URL);
		} 
		throw new RuntimeException("Existing Che Server is unreachable");
	}
	
	private static boolean isServerReachable(String URL) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection connection = (HttpURLConnection) new URL(URL).openConnection();
            connection.setRequestMethod("HEAD");
            return (connection.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            return false;
        }
    }
}
