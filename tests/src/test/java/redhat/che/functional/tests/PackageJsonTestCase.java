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
package redhat.che.functional.tests;

import com.redhat.arquillian.che.CheWorkspaceManager;
import com.redhat.arquillian.che.config.CheExtensionConfiguration;
import com.redhat.arquillian.che.provider.CheWorkspaceProvider;
import com.redhat.arquillian.che.resource.CheWorkspace;
import com.redhat.arquillian.che.resource.CheWorkspaceStatus;
import com.redhat.arquillian.che.service.CheWorkspaceService;
import org.apache.log4j.Logger;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.*;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import redhat.che.functional.tests.customExceptions.MarkerNotPresentException;

import java.util.concurrent.TimeUnit;

import static com.redhat.arquillian.che.util.Constants.CREATE_WORKSPACE_REQUEST_NODEJS_JSON;

@RunWith(Arquillian.class)
public class PackageJsonTestCase extends AbstractCheFunctionalTest {
    private static final Logger logger = Logger.getLogger(CheWorkspaceManager.class);

    @ArquillianResource
    private static CheWorkspace firstWorkspace;

    @ArquillianResource
    private static CheWorkspaceProvider provider;

    @FindBy(className = "currentLine")
    private WebElement currentLine;

    private CheWorkspace nodejsWorkspace;
    private String token;

    private String jsonDependency = "\"serve-static\": \"1.7.1\" \n,";

    private String jsonExpectedError = "Package serve-static-1.7.1 is vulnerable: CVE-2015-1164 Open redirect vulnerability. Recommendation: use version";

    @Before
    public void setEnvironment(){
        nodejsWorkspace = provider.createCheWorkspace(CREATE_WORKSPACE_REQUEST_NODEJS_JSON);
        logger.info("Workspace with node.js project created.");
        token = "Bearer " + System.getProperty(CheExtensionConfiguration.KEYCLOAK_TOKEN_PROPERTY_NAME);
        String runningState = CheWorkspaceStatus.RUNNING.getStatus();
        CheWorkspaceService.waitUntilWorkspaceGetsToState(nodejsWorkspace, runningState, token);

        openBrowser(nodejsWorkspace);
    }

    @After
    public void resetEnvironment(){
        Assert.assertTrue(provider.stopWorkspace(nodejsWorkspace));
        Assert.assertTrue(provider.startWorkspace(firstWorkspace));
        CheWorkspaceService.deleteWorkspace(nodejsWorkspace, token);
    }

    @Test
    public void testPackageJsonBayesian() throws MarkerNotPresentException{
        openPackageJson();
        editorPart.codeEditor().setCursorToLine(12);
        editorPart.codeEditor().writeDependency(jsonDependency);
        Assert.assertTrue("Annotation error is not visible.", editorPart.codeEditor().verifyAnnotationErrorIsPresent(jsonExpectedError));
    }

    private void openPackageJson() {
        nodejsProject.getResource("package.json").open();
        Graphene.waitGui().withTimeout(90, TimeUnit.SECONDS).until().element(currentLine).is().visible();
    }

}
