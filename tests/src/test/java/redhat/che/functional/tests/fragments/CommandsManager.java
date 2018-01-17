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
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import redhat.che.functional.tests.utils.ActionUtils;

import static org.jboss.arquillian.graphene.Graphene.waitGui;

public class CommandsManager {

    @Drone
    private WebDriver driver;

    @Root
    private WebElement rootElement;

    @FindBy(id = "gwt-debug-commands-explorer")
    private WebElement commandExplorer;

    @FindBy(id = "commands_tree-button-add")
    private WebElement buildPlus;

    @FindBy(id = "gwt-debug-ActionButton/executeSelectedCommand-true")
    private WebElement executeCommandButton;

    public void executeCommand() {
        executeCommandButton.click();
    }

    private void addMvnBuild(){
        waitGui().until().element(buildPlus).is().visible();
        buildPlus.click();
        waitGui().until().element(By.xpath("//option[@value='mvn']")).is().visible();
        WebElement mvn = driver.findElement(By.xpath("//option[@value='mvn']"));
        ActionUtils.doubleClick(driver, mvn);
    }

    public void openEditPanelForAddingBuildCommand() {
        addMvnBuild();
    }

    public void removeCommand(String testName) {
        CommandsManagerRow row = new CommandsManagerRow(testName, driver);
        row.removeCommand();
    }

    public boolean isCommandsExplorerOpen() {
        return commandExplorer.isDisplayed();
    }

}
