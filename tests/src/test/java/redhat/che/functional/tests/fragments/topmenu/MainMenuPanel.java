/******************************************************************************* 
 * Copyright (c) 2018 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
*/
package redhat.che.functional.tests.fragments.topmenu;

import static redhat.che.functional.tests.utils.ActionUtils.click;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.findby.JQuery;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * id = "gwt-debug-mainMenuPanel"
 */
public class MainMenuPanel {

    @Drone
    private WebDriver driver;

    @FindBy(id = "gwt-debug-MenuItem/profileGroup-true")
    private WebElement profileItem;

    @FindBy(id = "gwt-debug-MenuItem/git-true")
    private WebElement gitItem;

    @FindBy(id = "gwt-debug-MenuItem/workspaceGroup-true")
    private WebElement workspaceItem;

    @FindBy(id = "gwt-debug-command_toolbar-button_Run")
    private WebElement runButton;

    public void clickProfile(){
        click(driver, profileItem);
    }

    public void clickGit(){
        click(driver, gitItem);
    }
    
    public void clickWorkspace() {
    	click(driver, workspaceItem);
    }

    public void clickRunButton() { click(driver, runButton); }

    public void selectCommand(String commandLabel) {
        WebElement command = driver.findElement(ByJQuery.selector("#commandsPopup > div > div > div > div:contains('" + commandLabel + "')"));
        //the div containing command name is not clickable -> selecting clickable parent.
        WebElement parentElement = command.findElement(By.xpath("./.."));
        Graphene.waitGui().until().element(parentElement).is().clickable();
        parentElement.click();
    }
}
