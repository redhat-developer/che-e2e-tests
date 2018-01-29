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
import org.arquillian.extension.recorder.screenshooter.Screenshooter;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.runner.RunWith;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import redhat.che.functional.tests.fragments.EditorPart;
import redhat.che.functional.tests.fragments.Project;
import redhat.che.functional.tests.utils.Constants;

import java.util.concurrent.TimeUnit;

@RunWith(Arquillian.class)
public abstract class AbstractCheFunctionalTest {
    private static final Logger LOG = Logger.getLogger(AbstractCheFunctionalTest.class);

    @Drone
    protected WebDriver driver;

    @FindByJQuery("#gwt-debug-projectTree > div:contains('" + Constants.VERTX_PROJECT_NAME + "'):first")
    protected Project vertxProject;

    @FindByJQuery("#gwt-debug-projectTree > div:contains('" + Constants.NODEJS_PROJECT_NAME + "'):first")
    protected Project nodejsProject;

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

    @ArquillianResource
    private static Screenshooter screenshooter;

    private static short screenshotsTaken = 0;

    protected void openBrowser() {
        openBrowser(workspace);
    }

    protected void openBrowser(CheWorkspace wkspc) {
        LOG.info("Opening browser");
        driver.get(wkspc.getIdeLink());
//        driver.manage().window().maximize(); // Causes crash with Selenium on Xvfb - no window manager present
        screenshooter.setScreenshotTargetDir("target/screenshots");
        Graphene.waitGui().withTimeout(30, TimeUnit.SECONDS).until().element(loginPageOrworkspaceIsRunningPopup).is().visible();
        if ("username".equals(loginPageOrworkspaceIsRunningPopup.getAttribute("id"))) {
            login();
            waitUntilWorkspaceIsRunningElseRefresh();
        }
        Graphene.waitGui().until().element(workspaceIsRunningPopup).is().not().visible();
    }

    /**
     * Workarnound for https://github.com/openshiftio/openshift.io/issues/1304.
     * Should be removed once resolved.
     */
    private void waitUntilWorkspaceIsRunningElseRefresh() {
        Graphene.waitGui().withTimeout(3, TimeUnit.MINUTES).until(driver -> {
            try {
                Graphene.waitGui().until().element(loginPageOrworkspaceIsRunningPopup).is().visible();
                return true;
            } catch (WebDriverException e) {
                try {
                    driver.switchTo().alert().accept();
                } catch (NoAlertPresentException ex) {
                    // Alert didn't come up. Do nothing.
                }
                driver.navigate().refresh();
                return false;
            }
        });

    }

    private void login() {
        LOG.info("Logging in");
        usernameField.sendKeys(Constants.OSIO_USERNAME);
        passwordField.sendKeys(Constants.OSIO_PASSWORD);
        loginButton.click();
    }

    protected static void takeScreenshot(String fileName) {
        screenshotsTaken++;
        screenshooter.takeScreenshot(fileName + "_" + screenshotsTaken + ".png");
    }
    
}
