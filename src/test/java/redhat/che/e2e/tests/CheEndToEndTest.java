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

import redhat.che.e2e.tests.server.CheServerFactory;
import redhat.che.e2e.tests.workspace.CheWorkspace;
import redhat.che.e2e.tests.workspace.CheWorkspaceFactory;
import redhat.che.e2e.tests.workspace.CheWorkspaceService;
import redhat.che.e2e.tests.workspace.CheWorkspaceStatus;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(JUnit4.class)
public class CheEndToEndTest {

	private static final Logger logger = Logger.getLogger(CheEndToEndTest.class);
	
	public static final String CHE_SERVER_PROPERTY_NAME = "cheServerURL";
	
	public static final String WORKSPACE_JSON = "src/main/resources/create-ws.json";

	public static final String DEFAULT_CHE_URL = "http://demo.che.ci.centos.org";

	private static WebDriver driver;
	private static ChromeDriverService chromeService;
	
	private static CheWorkspace workspace;

	public static void setUpEnvVars() {
		if (getCheServerURL() == null) {
			logger.info("cheServerURL property is empty. Setting up default URL for Che server to " + DEFAULT_CHE_URL);
			System.setProperty(CHE_SERVER_PROPERTY_NAME, DEFAULT_CHE_URL);
		}
	}

	@BeforeClass
	public static void setUp() {
		setUpEnvVars();
		SeleniumProvider.setUpSeleniumChromeDriver();
		chromeService = SeleniumProvider.startChromeDriverService();
	}

	@Test
	public void testFirstCheScenario() {
		// First step - getting a Che Server (che-starter)
		CheServerFactory.getCheServer(ObjectState.EXISTING, getCheServerURL());

		// Second step, part A - create a new workspace (che-starter)
		workspace = CheWorkspaceFactory.getCheWorkspace(ObjectState.CUSTOM,
				getCheServerURL(), WORKSPACE_JSON);
		
		// Second step, part B - importing a project to workspace (che-starter) 
		logger.info("Deploying a project to workspace " + workspace.getName() + 
				" accessible at " + workspace.getWorkspaceURL());
		
		
		// TODO Add project to a workspace
		
		// to test workspace via selenium
		// setWebDriver(URL);
		// doMagic();
		// closeWebDriver();
		
		// Third step - run a single Test class and check results
		
		// Fourth step - open Che workspace and navigate to a project file - NOT
		// a test

		// Fifth step - do some Bayesian incompatible change, correct it via
		// codeAssist/contextAssist/whatever

		// Sixth step - commit and push
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

	private static void setWebDriver(String URL) {
		DesiredCapabilities capabilities = DesiredCapabilities.chrome();
		capabilities.setCapability("networkConnectionEnabled", "true");
		driver = new RemoteWebDriver(chromeService.getUrl(), capabilities);
	}
	
	private static void closeWebDriver() {
		if (driver != null) {
			driver.close();
		}
	}
	
	private static String getCheServerURL() {
		return System.getProperty(CHE_SERVER_PROPERTY_NAME);
	}
	
	// DIV ID to whole workspace
	// codenvyIdeWorkspaceViewImpl
	
	// DIV ID to project explorer
	// gwt-debug-projectTree
	
	// Projects have attribute "path" "project" class=GJHBXB5BACB id=gwt-uid-XXX
	
	// ID Of Context menu of a project
	// menu-lock-layer-id
	
	// Single class JUnit run context menu item ID
	// gwt-debug-contextMenu/Test/TestActionRunClassContext

	@AfterClass
	public static void cleanUp() {
		if (driver != null) {
			driver.close();
		}
		if (chromeService != null && chromeService.isRunning()) {
			SeleniumProvider.stopChromeDriverService(chromeService);
		}
		if (workspace != null) {
			if (CheWorkspaceService.getWorkspaceStatus(workspace).equals(CheWorkspaceStatus.RUNNING.getStatus())) {
				logger.info("Stopping workspace with ID " + workspace.getId());
				CheWorkspaceService.stopWorkspace(workspace);
			}
			logger.info("Deleting workspace with ID " + workspace.getId());
			CheWorkspaceService.deleteWorkspace(workspace);
		}
	}
}
