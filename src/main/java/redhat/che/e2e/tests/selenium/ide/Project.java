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
 * API for Projects in Project explorer.
 * 
 * @author mlabuda@redhat.com
 *
 */
public class Project extends AbstractResource {
    
    public Project(WebDriver driver, WebElement project) {
        super(driver, project);
    }
    
    /**
     * Gets project item by its path. Path consists of project items names.
     * 
     * @param path path consisting of project items names
     * @return project item if exists, null otherwise
     */
    public ProjectItem getProjectItem(String... path) {
        AbstractResource resource = this;
        for (int i=0; i < path.length -1; i++) {
            resource = getProjectItem(resource, path[i]);
        }
        if (!resource.isExpanded()) {
            resource.expand();
        }
        return getProjectItem(resource, path[path.length -1]);
    }
    
    private ProjectItem getProjectItem(AbstractResource resource, String name) {
        if (!resource.isExpanded()) {
            resource.expand();
        }
        return new ProjectItem(driver, resource.getResourceElement(name));
    }
}
