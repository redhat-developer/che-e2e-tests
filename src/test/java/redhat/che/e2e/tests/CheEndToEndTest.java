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

import org.apache.log4j.Logger;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import redhat.che.e2e.tests.provider.CheWorkspaceProvider;
import redhat.che.e2e.tests.resource.CheWorkspace;
import redhat.che.e2e.tests.resource.CheWorkspaceStatus;
import redhat.che.e2e.tests.selenium.ide.Labels;
import redhat.che.e2e.tests.selenium.ide.Popup;
import redhat.che.e2e.tests.selenium.ide.ProjectExplorer;
import redhat.che.e2e.tests.selenium.ide.TestResultsView;
import redhat.che.e2e.tests.service.CheWorkspaceService;

import static redhat.che.e2e.tests.Constants.CHE_STARTER_URL;
import static redhat.che.e2e.tests.Constants.CREATE_WORKSPACE_REQUEST_JSON;
import static redhat.che.e2e.tests.Constants.KEYCLOAK_TOKEN;
import static redhat.che.e2e.tests.Constants.OPENSHIFT_MASTER_URL;
import static redhat.che.e2e.tests.Constants.OPENSHIFT_NAMESPACE;
import static redhat.che.e2e.tests.Constants.PATH_TO_TEST_FILE;
import static redhat.che.e2e.tests.Constants.PRESERVE_WORKSPACE_PROPERTY_NAME;
import static redhat.che.e2e.tests.Constants.PROJECT_NAME;

@RunWith(Arquillian.class)
public class CheEndToEndTest {

	private static final Logger logger = Logger.getLogger(CheEndToEndTest.class);

	@Drone
	private WebDriver driver;
	
	private static CheWorkspace workspace;
	
	@Test
	public void testCreateWorkspaceAndHandleProject() {
		logger.info("Calling che starter to create a new workspace on OpenShift");

		workspace = CheWorkspaceProvider.createCheWorkspace(CHE_STARTER_URL, OPENSHIFT_MASTER_URL,
				KEYCLOAK_TOKEN, CREATE_WORKSPACE_REQUEST_JSON, OPENSHIFT_NAMESPACE);
		logger.info("Workspace successfully created.");

		logger.info("Waiting until workspace starts");
		CheWorkspaceService.waitUntilWorkspaceGetsToState(workspace, CheWorkspaceStatus.RUNNING.getStatus());

		driver.get(workspace.getIdeLink());
		// Running single JUnit Class
		logger.info("Running JUnit test class on the project");
		runTest(PROJECT_NAME);
		checkTestResults();
	}

	private void runTest(String projectName) {
		ProjectExplorer explorer = new ProjectExplorer(driver);
		explorer.selectItem(PATH_TO_TEST_FILE);		
		explorer.openContextMenuOnItem(PATH_TO_TEST_FILE);
		explorer.selectContextMenuItem(Labels.ContextMenuItem.TEST, Labels.ContextMenuItem.JUNIT_CLASS);
        
		// Wait until tests finish
		Popup testsPopup = new Popup(driver);
        testsPopup.waitUntilExists(Popup.RUNNING_TESTS_TITLE, 20);
        testsPopup.waitWhileExists(Popup.RUNNING_TESTS_TITLE, 100);
        testsPopup.waitUntilExists(Popup.SUCCESSFULL_TESTS_TITLE, 10);
	}

	private void checkTestResults() {
		TestResultsView testView = new TestResultsView(driver);
		testView.open();
		testView.assertLatestTestRunPassed();
	}
	
	private static boolean shouldNotDeleteWorkspace() {
		String preserveWorkspaceProperty = System.getProperty(PRESERVE_WORKSPACE_PROPERTY_NAME);
		if (preserveWorkspaceProperty == null) {
			return false;
		}
		if (preserveWorkspaceProperty.toLowerCase().equals("true")) {
			return true;
		} else {
			return false;
		}
	}
	
	@AfterClass
	public static void cleanUp() {
		if (workspace != null && !shouldNotDeleteWorkspace()) {
			if (CheWorkspaceService.getWorkspaceStatus(workspace).equals(CheWorkspaceStatus.RUNNING.getStatus())) {
				logger.info("Stopping workspace");
				CheWorkspaceService.stopWorkspace(workspace);
			}
			logger.info("Deleting workspace");
			CheWorkspaceService.deleteWorkspace(workspace);
		} else {
			logger.info("Property to preserve workspace is set to true, skipping workspace deletion");
		}
	}
}
