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

import redhat.che.e2e.tests.ObjectState;
import redhat.che.e2e.tests.Utils;
import redhat.che.e2e.tests.resource.CheServer;

public class CheServerFactory {

	private static final Logger logger = Logger.getLogger(CheServerFactory.class);
	
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
		logger.info("Trying to reach an existing Che server at " + URL);
		if (Utils.isURLReachable(URL)) {
			logger.info("Che server is accessible at " + URL);
			return new CheServer(URL);
		} 
		logger.error("Che server is not accessible at " + URL);
		throw new RuntimeException("Existing Che Server is unreachable");
	}
	
	
}
