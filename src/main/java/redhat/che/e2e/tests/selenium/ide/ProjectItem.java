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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Represents project item in Project explorer
 * 
 * @author mlabuda@redhat.com
 *
 */
public class ProjectItem extends AbstractResource {

    public ProjectItem(WebDriver driver, WebElement projectItem) {
        super(driver, projectItem);
    }
    
}
