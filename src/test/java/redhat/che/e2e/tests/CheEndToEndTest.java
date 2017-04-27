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
import static redhat.che.e2e.tests.Constants.KEYCLOAK_TOKEN;
import static redhat.che.e2e.tests.Constants.OPENSHIFT_MASTER_URL;
import static redhat.che.e2e.tests.Constants.OPENSHIFT_NAMESPACE;
import static redhat.che.e2e.tests.Constants.OPENSHIFT_TOKEN;
import static redhat.che.e2e.tests.Constants.PATH_TO_TEST_FILE;
import static redhat.che.e2e.tests.Constants.PRESERVE_WORKSPACE_PROPERTY_NAME;
import static redhat.che.e2e.tests.Constants.PROJECT_NAME;

import org.apache.log4j.Logger;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import redhat.che.e2e.tests.provider.CheWorkspaceProvider;
import redhat.che.e2e.tests.resource.CheWorkspace;
import redhat.che.e2e.tests.resource.CheWorkspaceStatus;
import redhat.che.e2e.tests.selenium.ide.GitMenu;
import redhat.che.e2e.tests.selenium.ide.Labels;
import redhat.che.e2e.tests.selenium.ide.Popup;
import redhat.che.e2e.tests.selenium.ide.Project;
import redhat.che.e2e.tests.selenium.ide.ProjectExplorer;
import redhat.che.e2e.tests.selenium.ide.ProjectItem;
import redhat.che.e2e.tests.selenium.ide.PushToRemoteWindow;
import redhat.che.e2e.tests.selenium.ide.TestResultsView;
import redhat.che.e2e.tests.service.CheWorkspaceService;

@RunWith(Arquillian.class)
public class CheEndToEndTest {

    private static final Logger logger = Logger.getLogger(CheEndToEndTest.class);

    @Drone
    private WebDriver driver;

    private static CheWorkspace workspace;

    @Before
    public void checkRunParams() {
        StringBuilder sb = new StringBuilder();
        if (CHE_STARTER_URL == null) {
            sb.append("Che starter URL cannot be null. Set property " + Constants.CHE_STARTER_PROPERTY_NAME
                    + " and rerun tests\n");
        }
        if (OPENSHIFT_MASTER_URL == null) {
            sb.append("OpenShift master URL cannot be null. Set property "
                    + Constants.OPENSHIFT_MASTER_URL_PROPERTY_NAME + "and rerun tests\n");
        }
        if (KEYCLOAK_TOKEN == null && OPENSHIFT_TOKEN == null) {
            sb.append("Keycloak and OpenShift tokens are null. Set either " + Constants.KEYCLOAK_TOKEN_PROPERTY_NAME
                    + " or " + Constants.OPENSHIFT_TOKEN_PROPERTY_NAME + " and rerun tests\n");
        }
        if (sb.length() > 0) {
            Assert.fail(sb.toString());
        }
    }

    @Test
    public void testCreateWorkspaceAndHandleProject() {
        logger.info("Calling che starter to create a new workspace on OpenShift");

        if (Constants.KEYCLOAK_TOKEN == null) {
            logger.info("Creating Che workspace via Che-starter OpenShift endpoint");
            workspace = CheWorkspaceProvider.createCheWorkspaceOSO(CHE_STARTER_URL, OPENSHIFT_MASTER_URL, OPENSHIFT_TOKEN,
                    CREATE_WORKSPACE_REQUEST_JSON, OPENSHIFT_NAMESPACE);
        } else {
            logger.info("Creating Che workspace via Che-starter Keycloak endpont");
            workspace = CheWorkspaceProvider.createCheWorkspace(CHE_STARTER_URL, OPENSHIFT_MASTER_URL, KEYCLOAK_TOKEN,
                    CREATE_WORKSPACE_REQUEST_JSON, OPENSHIFT_NAMESPACE);
        }
        logger.info("Workspace successfully created.");

        logger.info("Waiting until workspace starts");
        CheWorkspaceService.waitUntilWorkspaceGetsToState(workspace, CheWorkspaceStatus.RUNNING.getStatus());

        driver.get(workspace.getIdeLink());

        // Running single JUnit Class
        logger.info("Running JUnit test class on the project");
        runTest(PROJECT_NAME);
        checkTestResults();

        // TODO here must go changes to source code, otherwise Assert few lines below fail
        
        logger.info("Commiting and pushing changes");
        new ProjectExplorer(driver).getProject(PROJECT_NAME).select();
        boolean successfullCommit = commitChanges();
        Assert.assertTrue("Changes were not successfully added to index. Either there is "
                + "nothing to add to index or it is broken.", successfullCommit);
        pushChanges();
    }

    private void runTest(String projectName) {
        ProjectExplorer explorer = new ProjectExplorer(driver);
        Project project = explorer.getProject(PROJECT_NAME);
        project.select();
        ProjectItem testClassItem = project.getProjectItem(PATH_TO_TEST_FILE);
        testClassItem.select();
        testClassItem.openMenu();
        testClassItem.selectMenu(Labels.ContextMenuItem.TEST, Labels.ContextMenuItem.JUNIT_CLASS);
    
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

    private boolean commitChanges() {
        GitMenu gitMenu = new GitMenu(driver);
        gitMenu.openMenu();
        return gitMenu.addToIndex();
    }

    private void pushChanges() {
        GitMenu gitMenu = new GitMenu(driver);
        gitMenu.openMenu();
        gitMenu.push();

        PushToRemoteWindow pushDialog = new PushToRemoteWindow(driver);
        pushDialog.toString();
        // TODO
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
