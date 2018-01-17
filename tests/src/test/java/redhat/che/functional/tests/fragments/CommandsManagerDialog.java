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

/*
 * div[id='commandsManagerView']
 */
public class CommandsManagerDialog {

    @Drone
    private WebDriver driver;

    @Root
    private WebElement root;

    @FindBy(id="gwt-debug-commandsManager-type-custom")
    private WebElement customCommandItem;
    
    @FindByJQuery("div:contains('cd ${current.project.path} && scl enable rh-maven33'):first")
    private WebElement commandTextDiv;
    
    @FindByJQuery("textarea#gwt-debug-arbitraryPageView-cmdLine")
    private WebElement textArea;
    
    @FindByJQuery("button#window-edit-commands-save")
    private WebElement saveButton;

    @FindByJQuery("#gwt-debug-categoryHeader-custom > span > span > span:gt(1)")
    private WebElement customPlus;

    @FindBy(id = "gwt-debug-arbitraryPageView-cmdLine")
    private WebElement commandInput;

    @FindByJQuery("input:text[class=gwt-TextBox]")
    private WebElement nameInput;

    @FindByJQuery("div:contains('Deleting command')")
    private WebElement deletingLoader;

    private static final String RUN_COMMAND = "cd ${current.project.path} && java -jar target/*.jar";
    
    /**
     * Updated run command of mvn for vertx application to be compatible with vertx booster application.
     * 
     */
    public void updateCommandForJavaJar() {
       Graphene.waitModel().until().element(customCommandItem).is().present();
       customCommandItem.click(); 
       
       Graphene.waitModel().until().element(textArea).is().present();
       textArea.clear();
       textArea.sendKeys(RUN_COMMAND);
       
       saveButton.click();
       
       Graphene.waitModel().until().element(saveButton).is().not().enabled();
    }

    /**
     * Create custom command named by "commandName" with instruction "command"
     *
     */
    public void addCustomCommand(String commandName, String command) {
        select(customPlus);

        //setting variables
        select(commandInput);
        commandInput.clear();
        commandInput.sendKeys(command);
        nameInput.clear();
        nameInput.sendKeys(commandName);

        guardAjax(saveButton).click();
    }

    /**
     * Deleting command named by "name". Name is unique - there should be only 1 command with that name through all categories.
     *
     */
    public void deleteCommand(String name){
        //creating focus on a row to delete
        waitModel().until().element(customPlus).is().visible();
        WebElement row = driver.findElement(By.xpath("//div[@id='gwt-debug-commandWizard']/div/div/div[5]/div[2]/div"));
        row.click();

        //clik on minus in the row
        waitModel().until().element(textArea).is().visible();
        WebElement rowMinus = driver.findElement(By.xpath("//div[@id='gwt-debug-commandWizard']/div/div/div[5]/div[2]/div/span/span"));
        select(rowMinus);

        //confirm deleting
        WebElement ok = driver.findElement(By.id("ask-dialog-ok"));
        guardAjax(ok).click();
    }

    /**
     * Closing command manage dialog.
     *
     */
    public void closeEditCommands(){
        WebElement close = driver.findElement(By.id("window-edit-commands-close"));
        guardNoRequest(close).click();
    }

    private void select(WebElement element) {
        waitModel().until().element(element).is().visible();
        new Actions(driver).click(element).build().perform();
    }
}
