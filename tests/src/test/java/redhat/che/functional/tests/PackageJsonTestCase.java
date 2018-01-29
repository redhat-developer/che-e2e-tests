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
import com.redhat.arquillian.che.annotations.Workspace;
import com.redhat.arquillian.che.provider.CheWorkspaceProvider;
import com.redhat.arquillian.che.resource.CheWorkspace;
import com.redhat.arquillian.che.resource.Stack;
import org.apache.log4j.Logger;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import redhat.che.functional.tests.customExceptions.BayesianNotFunctionalException;
import java.util.concurrent.TimeUnit;

@RunWith(Arquillian.class)
@Workspace(removeAfterTest = false, stackID = Stack.NODEJS)
public class PackageJsonTestCase extends AbstractCheFunctionalTest {
    private static final Logger LOG = Logger.getLogger(CheWorkspaceManager.class);

    @FindBy(className = "currentLine")
    private WebElement currentLine;

    private static final String jsonDependency = "\"serve-static\": \"1.7.1\" ,\n";
    private static final String jsonExpectedError = "Package serve-static-1.7.1 is vulnerable: CVE-2015-1164 Open redirect vulnerability. Recommendation: use version";
    private static final Integer jsonExpectedErrorLine = 12;
    private static final Integer jsonInjectionEntryPoint = 12;

    @Before
    public void setEnvironment(){
        openBrowser();
    }

    @After
    public void resetEnvironment(){
        editorPart.codeEditor().setCursorToLine(jsonInjectionEntryPoint);
        editorPart.codeEditor().deleteNextLines(1);
    }

    @Test(expected = BayesianNotFunctionalException.class)
    public void testPackageJsonBayesian() throws Throwable {
        openPackageJson();
        editorPart.codeEditor().setCursorToLine(jsonInjectionEntryPoint);
        editorPart.codeEditor().writeDependency(jsonDependency);
        try {
            Assert.assertTrue(
                "Annotation error is not visible.",
                editorPart.codeEditor().verifyAnnotationErrorIsPresent(jsonExpectedError, jsonExpectedErrorLine)
            );
        } catch (AssertionError e) {
            if (CheWorkspaceProvider.getConfiguration().getOsioUrlPart().equals(bayesianErrorExpectedURL)) {
                throw new BayesianNotFunctionalException(bayesianErrorNotVisible);
            } else {
                LOG.error("Annotation assert failed:"+e.getMessage());
                throw e;
            }
        }
    }

    private void openPackageJson() {
        nodejsProject.getResource("package.json").open();
        Graphene.waitGui().withTimeout(90, TimeUnit.SECONDS).until().element(currentLine).is().visible();
    }

}
