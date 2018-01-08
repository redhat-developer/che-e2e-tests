/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package redhat.che.functional.tests;

import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import redhat.che.functional.tests.fragments.CommandsEditor;
import redhat.che.functional.tests.fragments.CommandsManager;
import redhat.che.functional.tests.fragments.LeftBar;

import java.util.concurrent.TimeUnit;

import static org.jboss.arquillian.graphene.Graphene.guardAjax;
import static org.jboss.arquillian.graphene.Graphene.waitModel;

/**
 * Created by katka on 22/06/17.
 */

@RunWith(Arquillian.class)
public class MavenTestCase extends AbstractCheFunctionalTest{
    @FindBy(id="gwt-debug-leftPanel")
    private LeftBar leftBar;

    @FindBy(id="gwt-debug-navPanel")
    private CommandsManager commandsManager;

    @FindByJQuery("pre:contains('Total time')")
    private WebElement consoleEnds;

    @FindByJQuery("pre:contains('BUILD SUCCESS')")
    private WebElement buildSuccess;

    @FindBy(id = "gwt-debug-editorMultiPartStack-contentPanel")
    private CommandsEditor commandsEditor;

    @FindBy(id = "ask-dialog-ok")
    private WebElement okButton;

    private final String testName = "buildTest";
    private final String command = "cd ${current.project.path} && scl enable rh-maven33 'mvn clean install'";

    @Before
    public void setup(){
        openBrowser();
    }

    @After
    public void deleteCommand(){
        commandsManager.removeCommand(testName);
        guardAjax(okButton).click();
    }

    /**
     * Tries to build project.
     */
    @Test
    public void test_maven_build() {
        //creating build command in left commands panel
        if (!commandsManager.isCommandsExplorerOpen()) leftBar.openCommandsPart();
        commandsManager.openEditPanelForAddingBuildCommand();
        commandsEditor.waitTillEditorVisible();
        commandsEditor.addNewCommand(testName, command);
        commandsEditor.runOpenedCommand();

        //wait for end - if build first time, it last longer -> increasing timeout
        waitModel().withTimeout(2, TimeUnit.MINUTES).until().element(consoleEnds).is().visible();

        Assert.assertTrue(buildSuccess.isDisplayed());
    }
}
