/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package redhat.che.functional.tests.fragments;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Loader {

    private WebDriver driver;

    private interface Locators {
        String PREFIX_ID = "gwt-debug-";
        String LOADER_ID = PREFIX_ID + "loader-panel";
        String MESSAGE_CONTAINER_ID = PREFIX_ID + "loader-message";
    }

    public void setWebDriver(WebDriver driver){
        this.driver=driver;
    }

    @FindBy(id = Locators.LOADER_ID)
    private WebElement loader;

    @FindBy(id = Locators.MESSAGE_CONTAINER_ID)
    private WebElement messContainer;

    /** Waits for Loader to hide. */
    public void waitOnClosed() {
        //in this pace pause, because loader can appear not at once
        new WebDriverWait(driver, 180)
                .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.LOADER_ID)));
    }
}
