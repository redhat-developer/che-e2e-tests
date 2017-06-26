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

import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import static org.jboss.arquillian.graphene.Graphene.waitModel;

/**
 * Represents abstact resource in Project explorer.
 * 
 * @author mlabuda@redhat.com
 */
public abstract class AbstractResource {

    protected static String expandedFolderStyle = "display: block;";

    /**
     * Finds out whether a resource is expanded
     * 
     * @param resourceElement
     *            resource element containing attributes path, name etc.
     * @return true if resource is expanded, false otherwise
     */
    public boolean isExpanded() {
        waitModel().until().element(getResourceElement()).is().visible();
        return expandedFolderStyle
                .equals(getResourceElement().findElement(ByJQuery.selector("> div:last")).getAttribute("style"));
    }

    /**
     * Expands resource element.
     * 
     * @param driver
     *            web driver
     * @param resourceElement
     *            resource element to expand
     */
    public void expand() {
        if (!isExpanded()) {
            new Actions(getWebDriver()).doubleClick(getResourceElement().findElement(ByJQuery.selector("> div:first"))).build()
                    .perform();
            waitModel().until(p -> expandedFolderStyle
                    .equals(getResourceElement().findElement(ByJQuery.selector("> div:last")).getAttribute("style")));
        }
    }

    /**
     * Gets WebElement of child resource.
     * 
     * @param driver
     *            web driver
     * @param parentResource
     *            parent resource
     * @param segmentPath
     *            path segment of child
     * @return WebElemenet of child resource
     */
    protected WebElement getChildResourceElement(String segmentPath) {
        if (!isExpanded()) {
            expand();
        }
        return getResourceElement().findElement(
                ByJQuery.selector("div[path='" + getResourceElement().getAttribute("path") + "/" + segmentPath + "']"));
    }

    /**
     * Selects resource.
     * 
     */
    public void select() {
        waitModel().until().element(getResourceElement()).is().visible();
        new Actions(getWebDriver()).click(getResourceElement()).build().perform();
    }

    /**
     * Opens resource.
     * 
     */
    public void open() {
        waitModel().until().element(getResourceElement()).is().visible();
        new Actions(getWebDriver()).doubleClick(getResourceElement()).perform();
    }

    /**
     * Opens context menu on resource.
     */
    public void openContextMenu() {
        select();
        new Actions(getWebDriver()).contextClick(getResourceElement()).build().perform();
    }

    public abstract <T extends AbstractResource> T getResource(String name);
    
    /**
     * Gets WebElement of resource.
     * 
     * @return web element of resource
     */
    protected abstract WebElement getResourceElement();

    /**
     * Gets web driver for doing actions.
     * 
     * @return web driver
     */
    protected abstract WebDriver getWebDriver();
    
    /**
     * Checks whether a child resource with name exists or not
     * @return true if child resource with specified name exists, false otherwise
     */
    public abstract boolean resourceExists(String name);
}
