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
package redhat.che.e2e.tests.selenium.ide;

import org.openqa.selenium.By;
import org.openqa.selenium.By.ById;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Abstract resource in project explorer.
 * 
 * @author mlabuda@redhat.com
 *
 */
public abstract class AbstractResource {

    protected WebDriver driver;
    protected WebElement element;

    private String expandedStyle = "display: block;";
    private String collapsedStyle = "display: none;";

    public AbstractResource(WebDriver driver, WebElement element) {
        this.driver = driver;
        this.element = element;
    }

    /**
     * Selects resource.
     */
    public void select() {
        element.click();
    }

    /**
     * Expands resource.
     */
    public void expand() {
        doubleClick();
        new WebDriverWait(driver, 10)
                .until(attributeOfChildToBe(element, By.xpath("./child::div[2]"), "style", expandedStyle));
    }

    private static ExpectedCondition<Boolean> attributeOfChildToBe(final WebElement webElement, 
            final By locator, final String attribute, final String value) {
        return new ExpectedCondition<Boolean>() {
            private String currentValue = null;

            @Override
            public Boolean apply(WebDriver driver) {
                WebElement element = webElement.findElement(locator);
                currentValue = element.getAttribute(attribute);
                if (currentValue == null || currentValue.isEmpty()) {
                    currentValue = element.getCssValue(attribute);
                }
                return value.equals(currentValue);
            }

            @Override
            public String toString() {
                return String.format("value to be \"%s\". Current value: \"%s\"", value, currentValue);
            }
        };
    }

    private void refreshExplorer() {
        new WebDriverWait(driver, 10)
                .until(ExpectedConditions.visibilityOf(driver.findElement(new ById(ProjectExplorer.REFRESH_BUTTON_ID))))
                .click();
    }

    /**
     * Finds out whether a resources is expanded or not.
     * 
     * @return true if resource is expanded, false otherwise
     */
    public boolean isExpanded() {
        String state = element.findElement(By.xpath("./child::div[2]")).getAttribute("style");
        if (state == null || state.isEmpty() || state.equals(collapsedStyle)) {
            return false;
        }
        if (state.equals(expandedStyle)) {
            return true;
        }
        throw new RuntimeException("Cannot find state of element. Check whether style for collapsed/expanded"
                + " resources has not changed.");
    }

    /**
     * Open resource if resource is actionable.
     */
    public void open() {
        String actionable = element.getAttribute("actionable");
        if (actionable != null && actionable.equals("true")) {
            doubleClick();
        }
    }

    /**
     * Opens context menu on a resource.
     */
    public void openMenu() {
        Actions act = new Actions(driver);
        Action rClick = act.contextClick(element).build();
        rClick.perform();
        rClick.perform();

        new WebDriverWait(driver, 30)
                .until(ExpectedConditions.visibilityOfElementLocated(By.id(ProjectExplorer.CONTEXT_MENU_ID)));
    }

    /**
     * Selects context menu item with specified path. Context menu has to be
     * opened in order to select a specific context menu item.
     * 
     * @param pathToContextMenuItem
     *            path to context menu item in context menu
     */
    public void selectMenu(String... pathToContextMenuItem) {
        for (String item : pathToContextMenuItem) {
            new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.id(item))).click();
        }
        new WebDriverWait(driver, 10)
                .until(ExpectedConditions.invisibilityOfElementLocated(By.id(ProjectExplorer.CONTEXT_MENU_ID)));
    }

    private void doubleClick() {
        try {
            select();
            new Actions(driver).doubleClick(element).perform();
        } catch (WebDriverException ex) {
            refreshExplorer();
            new Actions(driver).doubleClick(element).perform();
        }
    }

    /**
     * Gets resource element. Element has to be expanded at first.
     * 
     * @param name
     *            value of name attribute
     * @return web element of child resource
     */
    protected WebElement getResourceElement(String name) {
        String xpath = String.format(".//div[@name='%s']", name);
        new WebDriverWait(driver, 10)
                .until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(element, By.xpath(xpath)));
        return element.findElement(By.xpath(xpath));
    }

}
