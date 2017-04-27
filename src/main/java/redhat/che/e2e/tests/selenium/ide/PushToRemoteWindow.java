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

/**
 * Represents a dialog opened after click on menu item Git &gt; Remotes... &gt; Push....
 * 
 * @author mlabuda@redhat.com
 *
 */
public class PushToRemoteWindow {

    private WebDriver driver;
    
    public PushToRemoteWindow(WebDriver driver) {
        this.driver = driver;
    }
}
