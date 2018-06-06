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
package redhat.che.functional.tests.fragments.window;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.By.ById;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * @author rhopp
 *
 */
public class BranchesWindow {

	@Root
	private WebElement root;

	@FindBy(id = "git-branches-create")
	private WebElement createButton;

	@FindBy(id = "git-branches-checkout")
	private WebElement checkoutButton;

	@FindBy(id = "git-branches-close")
	private WebElement closeButton;

	@FindByJQuery("body #gwt-debug-askValueDialog-window")
	private AskForValueDialog askForValueDialog;

	public void createBranch(String branchName) {
		Graphene.click(createButton);
		askForValueDialog.waitFormToOpen();
		askForValueDialog.typeAndWaitText(branchName);
		askForValueDialog.clickOkBtn();
	}

	public void checkoutBranch(String branchName) {
		selectBranch(branchName);
		checkoutButton.click();
	}

	public void selectBranch(String branchName) {
		ById branchElementSelector = new ById("gwt-debug-git-branches-" + branchName);
		Graphene.waitGui().until().element(branchElementSelector).is().clickable();
		WebElement branchElement = root.findElement(branchElementSelector);
		branchElement.click();
	}

	public void waitForBeingVisible() {
		Graphene.waitGui().until().element(root).is().visible();
	}

}
