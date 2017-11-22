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

import static org.jboss.arquillian.graphene.Graphene.*;

public class CommandsEditor {

    @Drone
    private WebDriver driver;

    @Root
    private WebElement root;

    @FindByJQuery("input:text[class*=gwt-TextBox]")
    private WebElement nameInput;

    @FindByJQuery("div[id*=\"orion-editor\" > div:nth-child(6) > div.textviewContent > div > span:nth-child(1)")
    private WebElement cmdInput;

    @FindBy(id = "gwt-debug-command-editor-button-save")
    private WebElement saveButton;

    @FindBy(id = "gwt-debug-command-editor-button-cancel")
    private WebElement cancelButton;


    public void addNewCommand(String testName, String command) {
        System.out.println("ahoj");
        nameInput.clear();
        nameInput.sendKeys(testName);

        cmdInput.clear();
        cmdInput.sendKeys(command);

        guardAjax(saveButton).click();
        cancelButton.click();
    }
}
