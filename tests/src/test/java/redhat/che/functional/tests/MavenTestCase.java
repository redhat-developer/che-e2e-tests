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

import com.redhat.arquillian.che.annotations.Workspace;
import com.redhat.arquillian.che.resource.Stack;
import org.apache.log4j.Logger;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import redhat.che.functional.tests.fragments.CommandsEditor;
import redhat.che.functional.tests.fragments.CommandsManager;
import redhat.che.functional.tests.fragments.LeftBar;
import java.util.concurrent.TimeUnit;

/**
 * Created by katka on 22/06/17.
 */

@RunWith(Arquillian.class)
@Workspace(stackID = Stack.VERTX)
public class MavenTestCase extends AbstractCheFunctionalTest{
    private static final Logger LOG = Logger.getLogger(MavenTestCase.class);

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
        LOG.info("Starting: " + this.getClass().getName());
        openBrowser();
    }

    @After
    public void deleteCommand(){
        commandsManager.removeCommand(testName);
        new Actions(driver).click(okButton).perform();
    }

    /**
     * Tries to build project.
     */
    @Test
    public void test_maven_build() {
        //creating build command in left commands panel
        if (!commandsManager.isCommandsExplorerOpen()) {
            leftBar.openCommandsPart();
        }
        commandsManager.openEditPanelForAddingBuildCommand();
        commandsEditor.waitTillEditorVisible();
        commandsEditor.addNewCommand(testName, command);
        commandsEditor.runOpenedCommand();

        //wait for end - if build first time, it last longer -> increasing timeout
        //further increased timeout. test failed just because build took longer.
        Graphene.waitModel().withTimeout(3, TimeUnit.MINUTES).until().element(consoleEnds).is().visible();

        Assert.assertTrue(buildSuccess.isDisplayed());
    }
    
}
