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
import org.junit.*;
import org.junit.runner.RunWith;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import redhat.che.functional.tests.customExceptions.MarkerNotPresentException;
import redhat.che.functional.tests.fragments.window.AskForValueDialog;
import redhat.che.functional.tests.utils.ActionUtils;

@RunWith(Arquillian.class)
public class AnalyticsErrorMarkersTestCase extends AbstractCheFunctionalTest {

    private static final Logger logger = Logger.getLogger(AnalyticsErrorMarkersTestCase.class);

    @FindBy(id = "gwt-debug-askValueDialog-window")
    private AskForValueDialog askForValueDialog;

    @FindBy(className = "currentLine")
    private WebElement currentLine;

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

    @Test(expected = MarkerNotPresentException.class)
    public void bayesianErrorShownOnOpenFile() throws MarkerNotPresentException {
        //creating errorneous dependency
        openPomXml();
        setCursorToLine(37);
        editorPart.codeEditor().writeDependencyIntoPom();
        Assert.assertTrue("Annotation error is not visible.", editorPart.codeEditor().verifyAnnotationErrorIsPresent());

        //checking if error markes is visible after re-opening the file
        editorPart.tabsPanel().closeActiveTab();
        openPomXml();
        setCursorToLine(37);
        try {
            Assert.assertTrue("Annotation error is not visible when reopening file.", editorPart.codeEditor().verifyAnnotationErrorIsPresent());
        } catch (TimeoutException e){
            throw new MarkerNotPresentException(e.getMessage());
        }
    }

    private void setCursorToLine(int line) {
        ActionUtils.openMoveCursorDialog(driver);
        askForValueDialog.waitFormToOpen();
        askForValueDialog.typeAndWaitText(line);
        askForValueDialog.clickOkBtn();
        askForValueDialog.waitFormToClose();
    }

    private void openPomXml() {
        project.getResource("pom.xml").open();
        Graphene.waitGui().until().element(currentLine).is().visible();
    }

}
