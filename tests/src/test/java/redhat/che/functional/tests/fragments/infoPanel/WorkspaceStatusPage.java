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

import java.util.List;

import org.apache.log4j.Logger;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * @author rhopp
 * 
 *         #gwt-debug-process-output-panel-holder
 */
public class WorkspaceStatusPage {

	private static final Logger LOG = Logger.getLogger(WorkspaceStatusPage.class);

	@Drone
	private WebDriver driver;

	@Root
	private WebElement root;

	@FindByJQuery("td:contains('Workspace machines successfully started and configured')")
	private WebElement machinesAreReady;

	/**
	 * Returns true only if all installers are reported as OK. Otherwise returns
	 * false.
	 * 
	 * @return
	 */
	public boolean areInstallerStatusesOK() {
		List<WebElement> insallerStatuses = driver.findElements(ByJQuery.selector("[id='installer-status']"));
		for (WebElement installerStatus : insallerStatuses) {
			if (!installerStatus.getText().equals("OK")) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns true if {@link #areInstallerStatusesOK()} returns true AND td
	 * containing "Workspace machines successfully started and configured" is
	 * visible
	 * 
	 * @return
	 */
	public boolean isWorkspaceRunning() {
		if (!areInstallerStatusesOK()) {
			return false;
		}
		return machinesAreReady.isDisplayed();
	}

	public boolean isDisplayed() {
		return root.isDisplayed();
	}

}
