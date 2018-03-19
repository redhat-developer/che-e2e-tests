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
package redhat.che.functional.tests.fragments.infoPanel;

import org.apache.log4j.Logger;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * @author rhopp
 * 
 * This class represents notification area in the bottom panel area in che.
 * 
 * #gwt-debug-notificationManager-mainPanel
 */
public class NotificationManager {
	
	private static final Logger LOG = Logger.getLogger(NotificationManager.class);

	@Drone
    private WebDriver driver;

    @Root
    private WebElement root;
    
    public WebElement getNotificationElement(String title) {
    	LOG.info(String.format("Retrieving WebElement of notification '%s'", title));
    	return driver.findElement(ByJQuery.selector(String.format("div:contains('%s')", title)));
    }
    
    public WebElement getRootElement() {
    	return root;
    }
    
}
