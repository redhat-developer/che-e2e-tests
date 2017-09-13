/******************************************************************************* 
 * Copyright (c) 2017 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
*/
package redhat.che.functional.tests;

import com.redhat.arquillian.che.resource.CheWorkspace;

import org.apache.log4j.Logger;
import org.jboss.arquillian.drone.api.annotation.Drone;
import redhat.che.functional.tests.fragments.EditorPart;
import redhat.che.functional.tests.fragments.Project;

import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.jboss.arquillian.graphene.Graphene.waitModel;
import static redhat.che.functional.tests.utils.Constants.OSIO_PASSWORD;
import static redhat.che.functional.tests.utils.Constants.OSIO_USERNAME;
import static redhat.che.functional.tests.utils.Constants.PROJECT_NAME;

@RunWith(Arquillian.class)
public abstract class AbstractCheFunctionalTest {
	private static final Logger LOG = Logger.getLogger(AbstractCheFunctionalTest.class);
	
    @Drone
    protected WebDriver driver;

    @FindByJQuery("#gwt-debug-projectTree > div:contains('" + PROJECT_NAME + "'):first")
    protected Project project;

    @FindBy(id = "gwt-debug-editorMultiPartStack-contentPanel")
    protected EditorPart editorPart;

    @FindByJQuery("#gwt-debug-popup-container:contains('Workspace is running')")
    private WebElement workspaceIsRunningPopup;

    @FindByJQuery("#username, #gwt-debug-popup-container:contains('Workspace is running')")
    private WebElement loginPageOrworkspaceIsRunningPopup;

    @FindBy(id = "username")
    private WebElement usernameField;

    @FindBy(id = "password")
    private WebElement passwordField;

    @FindBy(id = "kc-login")
    private WebElement loginButton;

    @ArquillianResource
    private static CheWorkspace workspace;

    protected void openBrowser() {
    	LOG.info("Opening browser");
        driver.get(workspace.getIdeLink());
        waitModel().until().element(loginPageOrworkspaceIsRunningPopup).is().visible();
        if ("username".equals(loginPageOrworkspaceIsRunningPopup.getAttribute("id"))) {
            login();
            waitModel().until().element(loginPageOrworkspaceIsRunningPopup).is().visible();
        }
        waitModel().until().element(workspaceIsRunningPopup).is().not().visible();
    }

    private void login() {
    	LOG.info("Logging in");
        usernameField.sendKeys(OSIO_USERNAME);
        passwordField.sendKeys(OSIO_PASSWORD);
        loginButton.click();
        //ByJQuery collapse = ByJQuery.selector("div:has(path[id='collapse-expand'])");
        //waitModel().withTimeout(40, SECONDS).until().element(collapse).is().visible();
        //driver.findElement(collapse).click();
    }
}
