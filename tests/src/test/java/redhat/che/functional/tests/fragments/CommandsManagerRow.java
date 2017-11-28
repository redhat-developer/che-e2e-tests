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
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CommandsManagerRow {

    @Drone
    private WebDriver driver;

    @Root
    private WebElement rootElement;

    private String rowId;

    public CommandsManagerRow(String row, WebDriver driver){
        rowId = "command_" + row;
        rootElement = driver.findElement(By.id(rowId));
        this.driver = driver;
    }

    public void click(){
        driver.findElement(By.id(rowId)).click();
    }

    public void removeCommand() {
        this.click();
        String selector = "#" + rowId + " #commands_tree-button-remove";
        driver.findElement(ByJQuery.selector(selector)).click();
    }

    public void duplicateCommand(){
        this.click();
        String selector = "#" + rowId + " #commands_tree-button-duplicate";
        driver.findElement(ByJQuery.selector(selector)).click();
    }
}
