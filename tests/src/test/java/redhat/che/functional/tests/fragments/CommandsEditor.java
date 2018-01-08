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

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import redhat.che.functional.tests.utils.ActionUtils;

import java.util.concurrent.TimeUnit;

import static org.jboss.arquillian.graphene.Graphene.guardAjax;
import static org.jboss.arquillian.graphene.Graphene.waitGui;

public class CommandsEditor {

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

    @FindBy(id = "gwt-debug-command-editor-button-save")
    private WebElement saveButton;

    @FindBy(id = "gwt-debug-command-editor-button-cancel")
    private WebElement cancelButton;

    @FindBy(id = "gwt-debug-command-editor-button-run")
    private WebElement runButton;

    public void addNewCommand(String testName, String command) {
        waitGui().until(webDriver -> webDriver.switchTo().activeElement().equals(cmdInput) || webDriver.switchTo().activeElement().equals(previewURL));
        nameInput.clear();
        nameInput.sendKeys(testName);
        cmdInput.click();
        ActionUtils.selectAll(driver);
        ActionUtils.deleteMarkedLines(driver);
        cmdInput.sendKeys(command);
        waitGui().withTimeout(10, TimeUnit.SECONDS).until("Save button was not enabled. Buildscript possibly already exists.").element(saveButton).is().enabled();
        guardAjax(saveButton).click();
    }

    public void runOpenedCommand() {
        waitGui().until().element(runButton).is().enabled();
        guardAjax(runButton).click();
    }

    public void waitTillEditorVisible() {
        waitGui().until("Script editor did not open").element(root).is().visible();
    }

}
