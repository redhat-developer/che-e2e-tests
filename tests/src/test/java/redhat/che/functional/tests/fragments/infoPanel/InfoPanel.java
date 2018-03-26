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
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.condition.element.WebElementConditionFactory;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * @author rhopp
 * 
 * This class represents panels in botom area in Che IDE.
 * 
 * #gwt-debug-infoPanel
 *
 */
public class InfoPanel {

	private static final Logger LOG = Logger.getLogger(InfoPanel.class);
	
	@Drone
    private WebDriver driver;

    @Root
    private WebElement infoPanelRoot;
    
    @FindBy(id = "gwt-debug-notificationManager-mainPanel")
    private NotificationManager notificationManager;
    
    @FindBy(id = "gwt-debug-consolesPanel")
    private ConsolesPanel consolesPanel;
    
    @FindBy(id = "gwt-debug-partButton-Events")
    private WebElement notificationButon;
    
    @FindBy(id = "gwt-debug-partButton-Processes")
    private WebElement processesButton; 

	public NotificationManager getNotificationManager() {
		LOG.info("Retrieving notification area.");
		if (new WebElementConditionFactory(notificationManager.getRootElement()).isVisible().apply(driver)) {
			return notificationManager;
		} else {
			switchToNotificationManager();
			return notificationManager;
		}
	}
	
	public ConsolesPanel getConsolesPanel() {
		LOG.info("Retrieving consoles panel.");
		if (new WebElementConditionFactory(consolesPanel.getRootElement()).isVisible().apply(driver)) {
			return consolesPanel;
		} else {
			switchToConsolesPanel();
			return consolesPanel;
		}
	}

	private void switchToNotificationManager() {
		LOG.info("Notification area not focused. Focusing.");
		notificationButon.click();
	}
	
	private void switchToConsolesPanel() {
		LOG.info("ConsolePanel not focused. Focusing.");
		Graphene.guardAjax(processesButton).click();
	}
}
