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
package redhat.che.functional.tests.fragments;

import org.apache.log4j.Logger;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import redhat.che.functional.tests.utils.ActionUtils;
import java.util.concurrent.TimeUnit;

public class CommandsEditor {
    private static final Logger LOG = Logger.getLogger(CommandsEditor.class);

    @Drone
    private WebDriver driver;

    @Root
    private WebElement root;

    @FindByJQuery("#gwt-debug-command-editor-button-run ~ input")
    private WebElement nameInput;

    @FindByJQuery("#command_editor-command_line .textviewContent")
    private WebElement cmdInput;

    @FindByJQuery("#command_editor-preview_url .textviewContent")
    private WebElement previewURL;

    @FindByJQuery("#gwt-debug-command-editor-button-save:not([disabled])")
    private WebElement saveButton;

    @FindByJQuery("#gwt-debug-command-editor-button-cancel")
    private WebElement cancelButton;

    @FindByJQuery("#gwt-debug-command-editor-button-run")
    private WebElement runButton;

    public void addNewCommand(String testName, String command) {
        LOG.info("Deleting and writing a new command.");
        Graphene.waitGui().until(webDriver -> webDriver.switchTo().activeElement().equals(cmdInput) || webDriver.switchTo().activeElement().equals(previewURL));
        nameInput.clear();
        nameInput.sendKeys(testName);
        cmdInput.click();
        ActionUtils.selectAll(driver);
        ActionUtils.deleteMarkedLines(driver);
        cmdInput.sendKeys(command);
        LOG.info("Saving command.");
        Graphene.waitGui().withTimeout(10, TimeUnit.SECONDS)
            .until("Save button was not enabled. Buildscript possibly already exists.")
            .element(saveButton).is().visible();
        new Actions(driver).click(saveButton).perform();
    }

    public void runOpenedCommand() {
        LOG.info("Running command.");
        Graphene.waitGui().withTimeout(10, TimeUnit.SECONDS)
            .until("Run button was not enabled. Buildscript possibly not saved.")
            .element(runButton).is().visible();
        new Actions(driver).click(runButton).perform();
    }

    public void waitTillEditorVisible() {
        Graphene.waitGui().until("Script editor did not open").element(root).is().visible();
    }

}
