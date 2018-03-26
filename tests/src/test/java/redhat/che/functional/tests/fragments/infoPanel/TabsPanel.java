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
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * @author rhopp
 * #gwt-debug-multiSplitPanel-tabsPanel
 */
public class TabsPanel {
	
	private static final Logger LOG = Logger.getLogger(TabsPanel.class);
	
	@Drone
    private WebDriver driver;

    @Root
    private WebElement root;
    
    @FindByJQuery(">div[focused]")
    private WebElement focusedTab;
    
    public void switchToTab(String tabTitle) {
    	if (focusedTab.getText().equals(tabTitle)) {
    		LOG.info("Requested tab is already active");
    	}else {
    		WebElement requestedTab = driver.findElement(ByJQuery.selector(String.format(">div:contains('%s')", tabTitle)));
    		Graphene.guardAjax(requestedTab).click();
    	}
    }
}
