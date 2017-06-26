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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

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
}
