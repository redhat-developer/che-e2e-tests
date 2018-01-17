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

import org.apache.log4j.Logger;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import redhat.che.functional.tests.customExceptions.MarkerNotPresentException;
import redhat.che.functional.tests.fragments.window.AskForValueDialog;

@RunWith(Arquillian.class)
public class AnalyticsErrorMarkersTestCase extends AbstractCheFunctionalTest {

    private static final Logger logger = Logger.getLogger(AnalyticsErrorMarkersTestCase.class);

    @FindBy(id = "gwt-debug-askValueDialog-window")
    private AskForValueDialog askForValueDialog;

    @FindBy(className = "currentLine")
    private WebElement currentLine;

    private String pomDependency =
            "<dependency>\n"
                    + "<groupId>ch.qos.logback</groupId>\n"
                    + "<artifactId>logback-core</artifactId>\n"
                    + "<version>1.1.10</version>\n"
                    + "</dependency>\n";

    private String pomExpectedError = "Package ch.qos.logback:logback-core-1.1.10 is vulnerable: CVE-2017-5929. Recommendation: use version ";

    @Before
    public void importProject(){
        openBrowser();
    }

    @After
    public void deleteDependency(){
        editorPart.codeEditor().hideErrors();
        setCursorToLine(37);
        editorPart.codeEditor().deleteNextLines(5);
        editorPart.codeEditor().waitUnitlPomDependencyIsNotVisible();
        editorPart.tabsPanel().waintUntilFocusedTabSaves();
    }

    @Test
    public void bayesianErrorShownOnOpenFile() throws MarkerNotPresentException {
        //creating errorneous dependency
        openPomXml();
        setCursorToLine(37);
        editorPart.codeEditor().writeDependency(pomDependency);
        Assert.assertTrue("Annotation error is not visible.", editorPart.codeEditor().verifyAnnotationErrorIsPresent(pomExpectedError));

        //checking if error markes is visible after re-opening the file
        editorPart.tabsPanel().closeActiveTab();
        openPomXml();
        setCursorToLine(37);

        Assert.assertTrue("Annotation error is not visible when reopening file.", editorPart.codeEditor().verifyAnnotationErrorIsPresent(pomExpectedError));
    }

    private void openPomXml() {
        vertxProject.getResource("pom.xml").open();
        Graphene.waitGui().until().element(currentLine).is().visible();
    }

}
