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
package redhat.che.functional.tests.fragments;

import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import redhat.che.functional.tests.utils.ActionUtils;

/**
 * Terminal view in tab Panel.
 * 
 * div[id='gwt-debug-consolesPanel']
 * 
 * @author mlabuda@redhat.com
 *
 */
public class Terminal {

    @Drone
    private WebDriver driver;

    @Root
    private WebElement root;

    @FindByJQuery("span:contains('Terminal')")
    private WebElement terminalTab;

    @FindBy(id = "gwt-debug-Terminal")
    private WebElement terminalView;

    @FindByJQuery("div[class*='terminal xterm xterm-theme-default'")
    private WebElement xterm;

    /**
     * Activates terminal view
     */
    public void activateView() {
        terminalTab.click();
    }

    /**
     * Executes command in terminal
     * 
     * @param command
     *            command to execute
     */
    public void executeCommands(String command) {
        ActionUtils.writeIntoElement(driver, xterm, command);
        xterm.sendKeys(Keys.ENTER);
    }

    /**
     * Finds out whether terminal contains specific text or not.
     * 
     * @return true if terminal contains specified text, false otherwise
     */
    public boolean contains(String text) {
        try {
            Graphene.waitModel().withTimeout(5, TimeUnit.SECONDS).until()
                    .element(new ByJQuery("div:contains('" + text + "')")).is().visible();
            return true;
        } catch (NoSuchElementException ex) {
            return false;
        }
    }

}
