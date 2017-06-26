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
package redhat.che.functional.tests.fragments.popup;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.jboss.arquillian.graphene.Graphene.waitModel;

/**
 * 
 * Root JQuery: div#gwt-debug-popup-container
 * 
 * @author mlabuda@redhat.com
 *
 */
public class Popup {

    @Drone
    private WebDriver driver;
    
    @Root
    private WebElement root;
    
    @FindByJQuery("div[title=\"Running Tests...\"]")
    private WebElement runningTests;
    
    @FindByJQuery("div[title=\"Test runner executed successfully\"]")
    private WebElement successfulTests;
    
    public void waitUntilTestsAreRunnig() {
        waitModel().until().element(runningTests).is().visible();
    }
    
    public void waitWhileTestsAreRunning() {
        waitModel().until().element(runningTests).is().not().visible();
    }
    
    public void waitUntilTestsAreFinishedSuccessfully() {
        waitModel().until().element(successfulTests).is().visible();
    }
}
