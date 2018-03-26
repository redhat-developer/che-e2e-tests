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
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * @author rhopp
 *
 *         #gwt-debug-consolesPanel
 *
 */
public class ConsolesPanel {

	private static final Logger LOG = Logger.getLogger(ConsolesPanel.class);

	@Drone
	private WebDriver driver;

	@Root
	private WebElement root;

	@FindBy(id = "gwt-debug-multiSplitPanel-tabsPanel")
	private TabsPanel tabsPanel;

	@FindByJQuery("gwt-debug-process-output-panel-holder div[active]")
	private WebElement activeTabDiv;

	@FindBy(id = "gwt-debug-process-output-panel-holder")
	private WorkspaceStatusPage workspaceStatusPage;

	public WebElement getRootElement() {
		return root;
	}

	public WebElement getActiveTabDiv() {
		return activeTabDiv;
	}

	public void activateTab(String tabTitle) {
		LOG.info(String.format("Switching to tab with title '%s'", tabTitle));
		tabsPanel.switchToTab(tabTitle);
	}

	public WorkspaceStatusPage getWorkspaceStatusPage() {
		LOG.info("Obtaining workspace status page fragment");
		try {
			if (workspaceStatusPage.isDisplayed()) {
				return workspaceStatusPage;
			} else {
				throw new RuntimeException(
						"Workspace status page is not displayed. You have to open and activate it first");
			}
		} catch (NoSuchElementException ex) {
			throw new NoSuchElementException("Workspace status page not found. You have to open and activate it first",
					ex);
		}
	}

}
