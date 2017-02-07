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
package redhat.che.e2e.tests;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import redhat.che.e2e.tests.server.CheServerFactory;
import redhat.che.e2e.tests.workspace.CheWorkspace;
import redhat.che.e2e.tests.workspace.CheWorkspaceAPI;
import redhat.che.e2e.tests.workspace.CheWorkspaceFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(JUnit4.class)
public class CheEndToEndTest {

	public static final String CHE_SERVER_PROP = "cheServerURL";
	public static final String WORKSPACE_JSON = "src/main/resources/create-ws.json";

	public static final String DEFAULT_CHE_URL = "http://demo.che.ci.centos.org";
	
	private static String workspaceId; 
	
	public static void setUpEnvVars() {
		if (System.getProperty(CHE_SERVER_PROP) == null) {
			System.out.println("cheServerURL property is empty. Setting up default URL for Che server to " +
					DEFAULT_CHE_URL);
			System.setProperty(CHE_SERVER_PROP, DEFAULT_CHE_URL);
		}
	}

	@BeforeClass
	public static void setUp() {
		setUpEnvVars();
	}
	
	@Test
	public void testFirstCheScenario() {
		// First step - getting a Che Server (che-starter)
		System.out.println("Creating a Che server");
		CheServerFactory.getCheServer(ObjectState.EXISTING, System.getProperty(CHE_SERVER_PROP));

		// Second step - create a new workspace (che-starter)
		System.out.println("Creating a new Che workspace");
		CheWorkspace ws = CheWorkspaceFactory.getCheWorkspace(ObjectState.CUSTOM, 
				System.getProperty(CHE_SERVER_PROP), WORKSPACE_JSON);
		workspaceId = ws.getId();
	
		// Third step - run a single Test class and check results
		
		// Fourth step - open Che workspace and navigate to a project file - NOT a test
	
		// Fifth step - do some Bayesian incompatible change, correct it via codeAssist/contextAssist/whatever
		
		// Sixth step - commit and push
	}
	
	@Test
	@Ignore("Not implemented yet")
	public void testSecondCheScenario() {
		// Che server is already running, or should be from previous step, because of test order
		// check its existence and proceed
		
		// Opens a workspace with opened file on a specific line... - context oriented development
		
		// Correct file
		
		// Commit and push
	}
	
	@AfterClass
	public static void cleanUp() {
		System.out.println("Stopping workspace with ID " + workspaceId);
		CheWorkspaceAPI.stopWorkspace(System.getProperty(CHE_SERVER_PROP), workspaceId);
		System.out.println("Deleting workspace with ID " + workspaceId);
		CheWorkspaceAPI.deleteWorkspace(System.getProperty(CHE_SERVER_PROP), workspaceId);
	}
}
