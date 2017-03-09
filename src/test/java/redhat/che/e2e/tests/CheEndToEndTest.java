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

import static redhat.che.e2e.tests.Constants.*;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import redhat.che.e2e.tests.factory.CheServerFactory;
import redhat.che.e2e.tests.factory.CheWorkspaceFactory;
import redhat.che.e2e.tests.resource.CheWorkspace;
import redhat.che.e2e.tests.resource.CheWorkspaceStatus;
import redhat.che.e2e.tests.selenium.SeleniumProvider;
import redhat.che.e2e.tests.selenium.ide.Labels;
import redhat.che.e2e.tests.selenium.ide.ProjectExplorer;
import redhat.che.e2e.tests.service.CheWorkspaceService;
import redhat.che.e2e.tests.service.ProjectService;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(JUnit4.class)
public class CheEndToEndTest {

	private static final Logger logger = Logger.getLogger(CheEndToEndTest.class);
	
	private static WebDriver driver;
	private static ChromeDriverService chromeService;
	
	private static CheWorkspace workspace;

	@BeforeClass
	public static void setUp() {
		SeleniumProvider.setUpSeleniumChromeDriver();
		chromeService = SeleniumProvider.startChromeDriverService();
	}

	@Test
	public void testFirstCheScenarioWithCheStarterAndOpenShiftInvolved() {
		logger.info("Calling che starter to create a new workspace on OpenShift");
		
		workspace = CheWorkspaceFactory.getCheWorkspace(ObjectState.NEW, CHE_STARTER_URL,
				OPENSHIFT_MASTER_URL, OPENSHIFT_TOKEN, CREATE_WORKSPACE_REQUEST_JSON);
		CheWorkspaceService.waitUntilWorkspaceGetsToState(workspace, CheWorkspaceStatus.RUNNING.getStatus());
		
		// Set web driver at the beginning of all Web UI tests
		setWebDriver(workspace.getWorkspaceIDEURL());
		
		// Third step - run a single Test class and check results
		logger.info("Running JUnit test class on the project");
		runProjectOnTest(PROJECT_NAME);
		
		// Fourth step - open Che workspace and navigate to a project file - NOT
		// a test

		// Fifth step - do some Bayesian incompatible change, correct it via
		// codeAssist/contextAssist/whatever

		// Sixth step - commit and push
		
		// Close web driver after all Web UI tests
		closeWebDriver();
	}
	
	@Test
	@Ignore
	public void testFirstCheScenarioWithDirectCheServerCalls() {
		// First step - getting a Che Server (che-starter)
		CheServerFactory.getCheServer(ObjectState.EXISTING, CHE_SERVER);

		// Second step, part A - create a new workspace (che-starter)
		workspace = CheWorkspaceFactory.getCheWorkspace(ObjectState.CUSTOM,
				CHE_SERVER, WORKSPACE_JSON);
		CheWorkspaceService.startWorkspace(workspace);
		
		// Second step, part B - importing a project to workspace (che-starter) 
		logger.info("Deploying Vert.x project to workspace " + workspace.getName() + 
				" accessible at " + workspace.getWorkspaceURL());	
		ProjectService.createNewProject(workspace, PROJECT_JSON);		
		
		// Set web driver at the beginning of all Web UI tests
		setWebDriver(workspace.getWorkspaceURL());
		
		// Third step - run a single Test class and check results
		logger.info("Running JUnit test class on the project");
		runProjectOnTest(PROJECT_NAME);
		
		// Fourth step - open Che workspace and navigate to a project file - NOT
		// a test

		// Fifth step - do some Bayesian incompatible change, correct it via
		// codeAssist/contextAssist/whatever

		// Sixth step - commit and push
		
		// Close web driver after all Web UI tests
		closeWebDriver();
	}

	@Test
	@Ignore("Not implemented yet")
	public void testSecondCheScenario() {
		// Che server is already running, or should be from previous step,
		// because of test order
		// check its existence and proceed

		// Opens a workspace with opened file on a specific line... - context
		// oriented development

		// Correct file

		// Commit and push
	}

	private static void runProjectOnTest(String projectName) {
		ProjectExplorer explorer = new ProjectExplorer(driver);
		explorer.selectItem(PATH_TO_TEST_FILE);		
		explorer.openContextMenuOnItem(PATH_TO_TEST_FILE);
		explorer.selectContextMenuItem(Labels.ContextMenuItem.TEST, Labels.ContextMenuItem.JUNIT_CLASS);
		
		// TODO Check results, blocked by not working JUnit test for vert.x Test class (hanging job)
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
				logger.info("Stopping workspace with ID " + workspace.getId());
				CheWorkspaceService.stopWorkspace(workspace);
			}
			logger.info("Deleting workspace with ID " + workspace.getId());
			CheWorkspaceService.deleteWorkspace(workspace);
		} else {
			logger.info("Property to preserve workspace is set to true, skipping workspace deletion");
		}
	}
}
