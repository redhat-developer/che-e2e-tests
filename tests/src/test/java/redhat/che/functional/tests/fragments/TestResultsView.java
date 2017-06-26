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

import static org.jboss.arquillian.graphene.Graphene.waitModel;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

/**
 * 
 * Root JQuery: div#gwt-debug-infoPanel
 * 
 * @author mlabuda@redhat.com
 *
 */
public class TestResultsView {

    @Drone
    private WebDriver driver;
    
    @Root
    private WebElement root;
    
    @FindByJQuery("div[id=\"gwt-debug-partButton-Test Results\"]")
    private WebElement testResultsToolItem;
    
    @FindByJQuery("span:contains(\"Test passed.\")")
    private WebElement passedTests;
    
    public void open() {
        waitModel().until().element(testResultsToolItem).is().visible();
        
        if (testResultsToolItem.getAttribute("class").split(" ").length == 2) {
            new Actions(driver).click(testResultsToolItem).build().perform();
        }
    }
 
    public void waitUntilTestsPassed() {
        waitModel().until().element(passedTests).is().visible();
    }
}
