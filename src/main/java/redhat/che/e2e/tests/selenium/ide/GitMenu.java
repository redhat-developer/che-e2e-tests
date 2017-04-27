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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Git menu at top of IDE.
 * 
 * @author mlabuda@redhat.com
 *
 */
public class GitMenu {

    private WebDriver driver;
    
    // Top menu item, td
    private static final String TOP_MENU_ITEM_ID = "gwt-debug-MenuItem/git-true";
    
    // Menu items, tr
    private static final String ADD_TO_INDEX_MENU_ITEM_ID = "gwt-debug-topmenu/Git/gitAddToIndex"; 
    private static final String REMOTES_MENU_ITEM_ID = "gwt-debug-topmenu/Git/gitRemoteGroup";
    private static final String PUSH_MENU_ITEM_ID = "gwt-debug-topmenu/Git/Remotes.../gitPush";
    
    // Tab ID, div
    private static final String GIT_INDEX_TAB_ID = "gwt-debug-multiSplitPanel-tabsPanel";
    
    // Output in console, div
    private static final String GIT_OUTPUT_LINES_ID = "gwt-debug-gitOutputConsoleLines";
    
    // Text in console, pre
    private static final String ADDED_TO_INDEX_TEXT = "Git index updated";
    
    public GitMenu(WebDriver driver) {
        this.driver = driver;
    }
    
    /**
     * Open Git menu located at top of IDE.
     */
    public void openMenu() {
        String xpath = String.format("//td[@id='%s']", TOP_MENU_ITEM_ID);
        expandMenuItem(xpath);
    }
      
    /**
     * Selects add to index menu item in Git menu. Git menu has to be opened in advance.
     * 
     * @return true if items were added successfully, false otherwise
     */
    public boolean addToIndex() {
        // Selects menu item
        String menuXpath = String.format("//tr[@id='%s']", ADD_TO_INDEX_MENU_ITEM_ID);
        clickMenuItem(menuXpath);
        
        // Waits for status tab to show
        String tabXpath = String.format("//div[@id='%s']", GIT_INDEX_TAB_ID);
        new WebDriverWait(driver, Timeouts.REDRAW)
            .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(tabXpath)));
        
        // Check status of addition to index
        try {
            String textXpath = String.format("//div[@id='%s']//pre[text()='%s']", 
                    GIT_OUTPUT_LINES_ID, ADDED_TO_INDEX_TEXT);
            driver.findElement(By.xpath(textXpath)).click();
            return true;
        } catch (NoSuchElementException ex) {
            return false;
        }
    }
    
    /**
     * Clicks <i>Remotes... &gt; Push...</i> menu item. It opens a {@link PushToRemoteWindow}
     * which has to be handled separately.
     *  
     */
    public void push() {
        String remotesXpath = String.format("//tr[@id='%s']", REMOTES_MENU_ITEM_ID);
        expandMenuItem(remotesXpath);
        String pushXpath = String.format("//tr[@id='%s']", PUSH_MENU_ITEM_ID);
        clickMenuItem(pushXpath);
    }
    
    /**
     * Clicks menu item. It is expected that a menu item after click disappear (e.g. operation is run
     * and menu is hidden).
     * 
     * @param xpath
     */
    private void clickMenuItem(String xpath) {
       expandMenuItem(xpath);
       new WebDriverWait(driver, Timeouts.REDRAW)
           .until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(xpath)));
    }
    
    /**
     * Expands a menu item. Menu item is expanded and it is expected that potential submenu items are displayed.
     * 
     * @param xpath
     */
    private void expandMenuItem(String xpath) {
        new WebDriverWait(driver, Timeouts.REDRAW)
            .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
        driver.findElement(By.xpath(xpath)).click();
    }
}
