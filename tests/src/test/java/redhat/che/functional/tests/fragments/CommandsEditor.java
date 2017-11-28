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
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import redhat.che.functional.tests.utils.ActionUtils;

import static org.jboss.arquillian.graphene.Graphene.*;

public class CommandsEditor {

    @Drone
    private WebDriver driver;

    @Root
    private WebElement root;

    @FindByJQuery("#gwt-debug-command-editor-button-run ~ input")
    private WebElement nameInput;

    @FindByJQuery("#command_editor-command_line .textviewContent")
    private WebElement cmdInput;

    @FindBy(id = "gwt-debug-command-editor-button-save")
    private WebElement saveButton;

    @FindBy(id = "gwt-debug-command-editor-button-cancel")
    private WebElement cancelButton;

    @FindBy(id = "gwt-debug-command-editor-button-run")
    private WebElement runButton;


    public void addNewCommand(String testName, String command) {
        waitGui().until().element(nameInput).is().visible();
        nameInput.clear();
        nameInput.sendKeys(testName);

        //when using .clear() method the previous text persists
        cmdInput = driver.findElement(By.xpath("//*[@id=\"command_editor-command_line\"]//*[@class=\"textviewContent\" ]"));
        cmdInput.click();
        ActionUtils.selectAll(driver);
        ActionUtils.deleteMarkedLines(driver);
        cmdInput.sendKeys(command);

        guardAjax(saveButton).click();
    }

    public void runOpenedCommand() {
        waitGui().until().element(runButton).is().enabled();
        guardAjax(runButton).click();
    }
}
