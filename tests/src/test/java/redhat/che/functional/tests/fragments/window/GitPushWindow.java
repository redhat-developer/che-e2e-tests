/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package redhat.che.functional.tests.fragments.window;

import static redhat.che.functional.tests.utils.ActionUtils.click;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

/**
 * id = "gwt-debug-git-remotes-push-window"
 */
public class GitPushWindow {

    @Drone
    private WebDriver driver;

    @FindBy(id = "git-remotes-push-push")
    private WebElement pushButton;
    
    @FindBy(id = "gwt-debug-git-remotes-push-localBranch")
    private WebElement localBranchElement;
    
    @FindBy(id = "gwt-debug-git-remotes-push-remoteBranch")
    private WebElement remoteBranchElement;
    

    public void push(String localBranchName, String remoteBranchName){
    	Graphene.waitGui().until().element(localBranchElement).is().visible();
    	Select localBranchSelect = new Select(localBranchElement);
    	localBranchSelect.selectByValue(localBranchName);
    	Select remoteBranchSelect = new Select(remoteBranchElement);
    	remoteBranchSelect.selectByValue(remoteBranchName);
        click(driver, pushButton);
    }

}
