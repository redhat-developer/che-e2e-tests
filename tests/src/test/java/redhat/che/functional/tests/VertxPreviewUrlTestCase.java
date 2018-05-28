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
package redhat.che.functional.tests;

import com.redhat.arquillian.che.annotations.Workspace;
import com.redhat.arquillian.che.resource.Stack;
import com.redhat.arquillian.che.rest.RequestType;
import com.redhat.arquillian.che.rest.RestClient;
import okhttp3.Response;
import org.apache.log4j.Logger;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import redhat.che.functional.tests.fragments.CommandsManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(Arquillian.class)
@Workspace(stackID = Stack.VERTX, requireNewWorkspace = true)
public class VertxPreviewUrlTestCase extends AbstractCheFunctionalTest {
    private static final Logger LOG = Logger.getLogger(VertxPreviewUrlTestCase.class);

    @FindBy(id = "gwt-debug-commands-explorer")
    private CommandsManager commandsManager;

    @FindByJQuery("pre:contains('Succeeded in deploying verticle')")
    private WebElement buildSuccess;

    private void waitUntilRunIsCompleted() {
        Graphene.waitGui().withTimeout(60, TimeUnit.SECONDS).until().element(buildSuccess).is().visible();
    }

    @Test
    @InSequence(1)
    public void run_project() {
        LOG.info("Starting: " + this.getClass().getName());
        openBrowser();
        waitUntilProjectImported("Project vertx-http-booster imported", 60);
        mainMenuPanel.clickRunButton();
        mainMenuPanel.selectCommand("run");
        waitUntilRunIsCompleted();
    }

    @Test
    @InSequence(2)
    public void test_preview_url() {
        List<WebElement> preview = driver.findElements(ByJQuery.selector("#gwt-debug-process-output-panel-holder div:contains('preview') ~ a"));
        int code = 503;
        long startTime = System.currentTimeMillis();
        while (code == 503) {
            RestClient client = new RestClient(preview.get(1).getAttribute("href"));
            Response response = client.sendRequest("", RequestType.GET);
            code = response.code();
            if (response.isSuccessful()) {
                try {
                    Assert.assertTrue(response.body().string().contains("Vert.x HTTP Booster"));
                } catch (IOException e) {
                    LOG.info("Can not parse body of response. Exception is:" + e.getStackTrace());
                }
            } else {
                LOG.info("Request was not successfull: " + code);
            }
            if (System.currentTimeMillis() - startTime > 60000) {
                Assert.fail("Service is unavailable for more than a minute.");
                break;
            }
        }
    }


}
