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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ProjectItem extends AbstractResource {

    private WebElement projectItemResource;
    private WebDriver driver;
    
    /**
     * Constructs a new project item.
     * 
     * @param projectItemResource project item resource element (div with name, path...)
     */
    public ProjectItem(WebDriver driver, WebElement projectItemResource) {
        this.projectItemResource = projectItemResource;
        this.driver = driver;
    }

    @Override
    protected WebElement getResourceElement() {
        return projectItemResource;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ProjectItem getResource(String name) {
        return new ProjectItem(getWebDriver(), getChildResourceElement(name));
    }

    @Override
    protected WebDriver getWebDriver() {
        return driver;
    }
}
