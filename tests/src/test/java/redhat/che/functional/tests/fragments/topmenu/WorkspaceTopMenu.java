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
package redhat.che.functional.tests.fragments.topmenu;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import redhat.che.functional.tests.fragments.popup.PopupMenu;

/**
 * @author rhopp
 *
 */
public class WorkspaceTopMenu extends PopupMenu{
	
    @FindBy(id = "gwt-debug-topmenu/Workspace/showWorkspaceStatus")
    private WebElement showStatus;

    public void showStatus() {
    	click(showStatus);
    }
    
}
