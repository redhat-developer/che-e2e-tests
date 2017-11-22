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
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import redhat.che.functional.tests.utils.ActionUtils;

import static org.jboss.arquillian.graphene.Graphene.guardAjax;
import static org.jboss.arquillian.graphene.Graphene.guardNoRequest;

/*
 * root div[id="gwt-debug-toolbarPanel"]
 */
public class DebugLeftPanel {

    @Drone
    private WebDriver driver;
    
    @Root
    private WebElement rootElement;

    @FindBy(id = "gwt-debug-partButton-Commands")
    private WebElement commandsPart;

    @FindBy(id = "commands_tree-button-add")
    private WebElement buildPlus;

    @FindBy(id = "gwt-debug-ActionButton/executeSelectedCommand-true")
    private WebElement executeCommandButton;

    @FindByJQuery("div[id='gwt-debug-dropDownHeader']:last")
    private WebElement commandsDropDownButton;

    /**
     * Clicks button to execute selected command.
     */



    public void executeCommand() {
        executeCommandButton.click();
    }

    private void openCommandsPart(){
        commandsPart.click();
    }

    private void addMvnBuild(){
        buildPlus = driver.findElement(By.id("commands_tree-button-add"));
        guardNoRequest(buildPlus).click();
        WebElement mvn = driver.findElement(By.xpath("//option[@value='mvn']"));
        ActionUtils.doubleClick(driver, mvn);
    }

    public void openEditPanelForAddingBuildCommand(String testName, String command) {
        openCommandsPart();
        addMvnBuild();

    }
}
