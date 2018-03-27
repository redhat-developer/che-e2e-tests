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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import redhat.che.functional.tests.utils.ActionUtils;
import java.util.concurrent.TimeUnit;

public class CommandsManager {

    @Drone
    private WebDriver driver;

    @Root
    private WebElement rootElement;
    
    @FindBy(id = "commands_tree-button-add")
    private WebElement buildPlus;

    // Specifically look for <selector> with size of 5 (fully loaded popup)
    @FindByJQuery("body .gwt-PopupPanel .gwt-ListBox[size='5']")
    private WebElement commandTypeListBoxLoaded;

    @FindByJQuery("body .gwt-PopupPanel .gwt-ListBox option[value='mvn']")
    private WebElement commandTypeMaven;

    @FindBy(id = "gwt-debug-ActionButton/executeSelectedCommand-true")
    private WebElement executeCommandButton;

    public void executeCommand() {
        executeCommandButton.click();
    }

    public void openEditPanelForAddingBuildCommand() {
        Graphene.waitGui().withTimeout(10, TimeUnit.SECONDS)
            .until("Could not locate add command button")
            .element(buildPlus).is().visible();
        buildPlus.click();
        Graphene.waitGui().withTimeout(10, TimeUnit.SECONDS)
            .until("Command type listbox did not load")
            .element(commandTypeListBoxLoaded).is().visible();
        Graphene.waitGui().withTimeout(10, TimeUnit.SECONDS)
            .until("Could not find command type Maven")
            .element(commandTypeMaven).is().visible();
        ActionUtils.doubleClick(driver, commandTypeMaven);
    }

    public void removeCommand(String testName) {
        CommandsManagerRow row = new CommandsManagerRow(testName, driver);
        row.removeCommand();
    }

    public boolean isCommandsExplorerOpen() {
    	try {
    		return rootElement.isDisplayed();
    	}catch (NoSuchElementException e) {
    		return false;
    	}
    }

}
