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
package redhat.che.e2e.tests.fragments;

import static org.jboss.arquillian.graphene.Graphene.waitModel;

import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

/**
 * 
 * Represent page fragments for context menu opened on a file
 * 
 * Root JQuery: table:has(tr#gwt-debug-contextMenu/newGroup)
 * 
 * @author mlabuda@redhat.com
 *
 */
public class ContextMenu extends PopupMenu {
    
    @FindByJQuery("nobr[id='contextMenu/Run Test']")
    private WebElement runTestMenuItem;
    
    @FindByJQuery("nobr[id='contextMenu/Run Test/JUnit Class']")
    private WebElement runTestClassMenuItem;
    
    public void selectRunJUnitClassTest() {
        select(runTestMenuItem);
        select(runTestClassMenuItem);
    }
    
    private void select(WebElement element) {
        waitModel().until().element(element).is().visible();
        new Actions(driver).click(element).build().perform();
    }
}