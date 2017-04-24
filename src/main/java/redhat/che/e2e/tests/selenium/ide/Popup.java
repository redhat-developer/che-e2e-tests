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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Popup {
    
    public static final String RUNNING_TESTS_TITLE = "Running Tests...";
    public static final String SUCCESSFULL_TESTS_TITLE = "Test runner executed successfully";

    private static final String DEBUG_POPUP_WINDOW_PREFIX = "gwt-debug-popup-titlegwt-uid-";
    
    private WebDriver driver;
    
    public Popup(WebDriver driver) {
        this.driver = driver;
    }
    
    public void waitUntilExists(String title, long timeout) {
        String locator = String.format("//div[starts-with(@id, '%s')]//div[@title='%s']", 
                DEBUG_POPUP_WINDOW_PREFIX, title);
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.withMessage("Wait expired while waiting until popup with title " + title + " gets available.");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
    }
    
    public void waitWhileExists(String title, long timeout) {
        String locator = String.format("//div[starts-with(@id, '%s')]//div[@title='%s']", DEBUG_POPUP_WINDOW_PREFIX,
                title);
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.withMessage("Wait expired while waiting while popup with title " + title + " gets available.");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(locator)));
    }
}
