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
import org.junit.*;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import redhat.che.functional.tests.fragments.CommandsEditor;
import redhat.che.functional.tests.fragments.CommandsManagerDialog;
import redhat.che.functional.tests.fragments.DebugLeftPanel;
import redhat.che.functional.tests.fragments.popup.DropDownMenu;

import java.util.concurrent.TimeUnit;

import static org.jboss.arquillian.graphene.Graphene.waitModel;

/**
 * Created by katka on 22/06/17.
 */

@RunWith(Arquillian.class)
public class MavenTestCase extends AbstractCheFunctionalTest{
    @FindBy(id="gwt-debug-leftPanel")
    private DebugLeftPanel leftPanel;

    @FindByJQuery("pre:contains('Total time')")
    private WebElement consoleEnds;

    @FindByJQuery("pre:contains('BUILD SUCCESS')")
    private WebElement buildSuccess;

    @FindBy(id = "menu-lock-layer-id")
    private DropDownMenu dropDownMenu;

    @FindByJQuery("div#commandsManagerView")
    private CommandsManagerDialog commandsManagerDialog;

    @FindBy(id = "gwt-debug-editorPartStack-contentPanel")
    private CommandsEditor commandsEditor;

    private final String testName = "buildTest";
    private final String command = "cd ${current.project.path} && scl enable rh-maven33 'mvn clean install'";

    @Before
    public void setup(){
        openBrowser();
        vertxProject.select();
    }

    @After
    public void deleteCommand(){
        //leftPanel.openCommandsPart();
        dropDownMenu.selectEditCommand();
        commandsManagerDialog.deleteCommand(testName);
    }

    /**
     * Tries to build project.
     */
    @Test
    public void test_maven_build() {
        //creating build command in left commands panel
        leftPanel.openEditPanelForAddingBuildCommand(testName, command);
        commandsEditor.addNewCommand(testName, command);
        /*
        dropDownMenu.selectEditCommand();
        commandsManagerDialog.addCustomCommand(testName, command);
        commandsManagerDialog.closeEditCommands();

        //running command (created command is automatically selected - no need to search for it in dropdown)
        leftPanel.executeCommand();

        //wait for end - if build first time, it last longer -> increasing timeout
        waitModel().withTimeout(2, TimeUnit.MINUTES).until().element(consoleEnds).is().visible();

        Assert.assertTrue(buildSuccess.isDisplayed());*/
    }
}
