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

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.WebElement;

public class DropDownMenu extends PopupMenu {

    @FindByJQuery("nobr[id='CommandsGroup/build']")
    private WebElement buildCommandItem;

    @FindByJQuery("nobr[id='CommandsGroup/run']")
    private WebElement runCommandItem;

    @FindByJQuery("nobr[id='CommandsGroup/Edit Commands...']")
    private WebElement editCommandItem;

    /**
     * Selects 'Edit Commands...' command in drop down menu.
     */
    public void selectEditCommand() {
        selectCommand(editCommandItem);
    }

    /**
     * Selects 'build' command in drop down menu.
     */
    public void selectBuildCommand() {
        selectCommand(buildCommandItem);
    }

    /**
     * Selects 'run' command in drop down menu.
     */
    public void selectRunCommand() {
        selectCommand(runCommandItem);
    }

    private void selectCommand(WebElement commandItem) {
        Graphene.waitModel().until().element(commandItem).is().visible();
        commandItem.click();
    }
}
