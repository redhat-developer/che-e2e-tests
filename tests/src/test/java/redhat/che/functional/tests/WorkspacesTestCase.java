package redhat.che.functional.tests;


import com.redhat.arquillian.che.CheWorkspaceManager;
import com.redhat.arquillian.che.config.CheExtensionConfiguration;
import com.redhat.arquillian.che.provider.CheWorkspaceProvider;
import com.redhat.arquillian.che.resource.CheWorkspace;
import com.redhat.arquillian.che.resource.CheWorkspaceStatus;
import com.redhat.arquillian.che.service.CheWorkspaceService;
import org.apache.log4j.Logger;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by katka on 03/07/17.
 */

@RunWith(Arquillian.class)
public class WorkspacesTestCase {
    private static final Logger logger = Logger.getLogger(CheWorkspaceManager.class);

    @ArquillianResource
    private static CheWorkspace firstWorkspace;

    @ArquillianResource
    private static CheWorkspaceProvider provider;

    String runningState;
    String stoppedState;
    CheWorkspace secondWorkspace;
    String token;

    @Before
    public void settingAttributes(){
        runningState = CheWorkspaceStatus.RUNNING.getStatus();
        stoppedState = CheWorkspaceStatus.STOPPED.getStatus();
        token = "Bearer " + System.getProperty(CheExtensionConfiguration.KEYCLOAK_TOKEN_PROPERTY_NAME);
    }

    @After
    public void resetWorkspaces(){
        Assert.assertTrue(provider.stopWorkspace(secondWorkspace));
        logger.info("Second workspace stopped");
        Assert.assertTrue(provider.startWorkspace(firstWorkspace));
        logger.info("First workspace started");
        CheWorkspaceService.deleteWorkspace(secondWorkspace, token);
    }

    @Test
    public void startSecondWorkspace() {
        Assert.assertNotNull(firstWorkspace);
        String status = CheWorkspaceService.getWorkspaceStatus(firstWorkspace, token);
        logger.info("Status of first workspace is: " +status);
        Assert.assertEquals("First workspace status should be RUNNING but is " + status, runningState, status);

        logger.info("Creating second workspace");
        secondWorkspace = provider.createCheWorkspace(null);
        CheWorkspaceService.waitUntilWorkspaceGetsToState(secondWorkspace, runningState, token);
        logger.info("Second workspace should be started");

        status = CheWorkspaceService.getWorkspaceStatus(secondWorkspace, token);
        Assert.assertEquals("Second workspace status should be RUNNING but is" + status, runningState, status);
        status = CheWorkspaceService.getWorkspaceStatus(firstWorkspace, token);
        Assert.assertEquals("First workspace status should be STOPPED but is " + status, stoppedState, status);
    }

}
