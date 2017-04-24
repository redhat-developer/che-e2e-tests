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

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class TestResultsView {

    private WebDriver driver;

    private static final String VIEW_TOOL_ITEM_ID = "gwt-debug-partButton-Test Results";
    private static final String VIEW_PANEL_ID = "gwt-debug-infoPanel";
    private static final String TOOLBAR_TITLE = "Test Results (Framework: JUnit4x)";
    
    private static final String PASSED_TESTS = "Test passed.";

    public TestResultsView(WebDriver driver) {
        this.driver = driver;
    }
    
    public void open() {
        WebElement webElement = driver.findElement(By.xpath("//div[@id='" + VIEW_TOOL_ITEM_ID + "']"));
        if (webElement.getAttribute("class").split(" ").length == 2) {
            webElement.click();
        }
        
        new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format("//div[@id='%s']//div[text()='%s']", VIEW_PANEL_ID, TOOLBAR_TITLE))));
    }
    
    public void assertLatestTestRunPassed() {
        try {
            new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format("//div[@id='%s']//span[text()='%s']", VIEW_PANEL_ID, PASSED_TESTS))));
        } catch (TimeoutException ex) {
            Assert.fail("Waiting for successful test run expired. Tests did not pass in required time period.");
        }
    }

}
