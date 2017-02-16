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

import redhat.che.e2e.tests.ObjectState;

public class CheWorkspaceFactory {
	
	/**
	 * Gets a Che workspace.
	 * To get a new Che workspace via CUSTOM way (direct calls of Che API), pass following arguments:
	 * <String cheServerURL> url of Che server to create workspace on
	 * <String pathToWorkspaceJSON> path to a json file containing workspace definition for REST call to Che
	 * @param state
	 * @param args
	 * @return Che workspace
	 */
	public static CheWorkspace getCheWorkspace(ObjectState state, String... args) {
		if (ObjectState.CUSTOM.equals(state)) {
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
			///////////////////////////////////
			//// HERE GOES CHE STARTER STUFF///
			//////////////////////////////////
			throw new UnsupportedOperationException("Creating a new workspace with Che starter is not supported yet");
		}
		throw new IllegalArgumentException("Unsupported Object state");
	}
}
