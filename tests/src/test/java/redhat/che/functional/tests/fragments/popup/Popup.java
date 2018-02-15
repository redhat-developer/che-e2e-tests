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
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.jboss.arquillian.graphene.Graphene.waitModel;

import java.util.concurrent.TimeUnit;

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
    
    public void waitForPopup(String popupTitle) {
    	waitModel().withTimeout(10, TimeUnit.SECONDS).until().element(ByJQuery.selector("div[title=\""+popupTitle+"\"]")).is().visible();
    }
}
