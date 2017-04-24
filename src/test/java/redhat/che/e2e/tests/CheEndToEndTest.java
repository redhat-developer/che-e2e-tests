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

import static redhat.che.e2e.tests.Constants.CHE_STARTER_URL;
import static redhat.che.e2e.tests.Constants.CREATE_WORKSPACE_REQUEST_JSON;
import static redhat.che.e2e.tests.Constants.OPENSHIFT_MASTER_URL;
import static redhat.che.e2e.tests.Constants.OPENSHIFT_NAMESPACE;
import static redhat.che.e2e.tests.Constants.KEYCLOAK_TOKEN;
import static redhat.che.e2e.tests.Constants.PATH_TO_TEST_FILE;
import static redhat.che.e2e.tests.Constants.PRESERVE_WORKSPACE_PROPERTY_NAME;
import static redhat.che.e2e.tests.Constants.PROJECT_NAME;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import redhat.che.e2e.tests.provider.CheWorkspaceProvider;
import redhat.che.e2e.tests.resource.CheWorkspace;
import redhat.che.e2e.tests.resource.CheWorkspaceLink;
import redhat.che.e2e.tests.resource.CheWorkspaceStatus;
import redhat.che.e2e.tests.selenium.SeleniumProvider;
import redhat.che.e2e.tests.selenium.ide.Labels;
import redhat.che.e2e.tests.selenium.ide.Popup;
import redhat.che.e2e.tests.selenium.ide.ProjectExplorer;
import redhat.che.e2e.tests.selenium.ide.TestResultsView;
import redhat.che.e2e.tests.service.CheWorkspaceService;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(JUnit4.class)
public class CheEndToEndTest {

	private static final Logger logger = Logger.getLogger(CheEndToEndTest.class);
	
	private static WebDriver driver;
	private static ChromeDriverService chromeService;
	
	private static CheWorkspaceLink workspaceLink;
	private static CheWorkspace workspace;

	@BeforeClass
	public static void setUp() {
		SeleniumProvider.setUpSeleniumChromeDriver();
		chromeService = SeleniumProvider.startChromeDriverService();
	}
	
	@Test
	public void testCreateWorkspaceAndHandleProject() {
		logger.info("Calling che starter to create a new workspace on OpenShift");
		
		workspaceLink = CheWorkspaceProvider.createCheWorkspace(CHE_STARTER_URL, OPENSHIFT_MASTER_URL, 
				KEYCLOAK_TOKEN, CREATE_WORKSPACE_REQUEST_JSON, OPENSHIFT_NAMESPACE);
		logger.info("Workspace successfully created.");
		workspace = CheWorkspaceProvider.getWorkspaceByLink(CHE_STARTER_URL, OPENSHIFT_MASTER_URL,
		        KEYCLOAK_TOKEN, OPENSHIFT_NAMESPACE, workspaceLink);
		
		logger.info("Waiting until workspace starts");
		CheWorkspaceService.waitUntilWorkspaceGetsToState(workspace, CheWorkspaceStatus.RUNNING.getStatus());
		
		// Set web driver at the beginning of all Web UI tests
		setWebDriver(workspaceLink.getURL());
		
		// Running single JUnit Class
		logger.info("Running JUnit test class on the project");
		runTest(PROJECT_NAME);
		checkTestResults();
		
		closeWebDriver();
	}

	private static void runTest(String projectName) {
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
	
	private static void checkTestResults() {
		TestResultsView testView = new TestResultsView(driver);
		testView.open();
		testView.assertLatestTestRunPassed();
	}
	
	private static void setWebDriver(String URL) {
		DesiredCapabilities capabilities = DesiredCapabilities.chrome();
		capabilities.setCapability("networkConnectionEnabled", "true");
		driver = new RemoteWebDriver(chromeService.getUrl(), capabilities);
		driver.navigate().to(URL);
	}
	
	private static void closeWebDriver() {
		if (driver != null) {
			driver.quit();
			driver = null;
		}
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
		if (driver != null) {
			try {
				driver.quit();
			} catch (Exception ex) { 
				// if something went wrong and driver couldnt quit, mostly bcs it is disposed
				logger.info("Driver could not be disposed. Probably it is already disposed.");
			}
		}
		if (chromeService != null && chromeService.isRunning()) {
			SeleniumProvider.stopChromeDriverService(chromeService);
		}
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
