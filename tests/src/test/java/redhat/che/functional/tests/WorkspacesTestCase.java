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
    private static CheWorkspace workspace;

    @ArquillianResource
    private static CheWorkspaceProvider provider;

    String runningState;
    String stoppedState;
    CheWorkspace firstWorkspace;
    CheWorkspace secondWorkspace;
    String token;

    @Before
    public void settingAttributes(){
        runningState = CheWorkspaceStatus.RUNNING.getStatus();
        stoppedState = CheWorkspaceStatus.STOPPED.getStatus();
        token = "Bearer " + System.getProperty(CheExtensionConfiguration.KEYCLOAK_TOKEN_PROPERTY_NAME);
    }

    @Test
    public void startSecondWorkspace(){
        Assert.assertNotNull(workspace);
        String status = CheWorkspaceService.getWorkspaceStatus(workspace, token);
        Assert.assertEquals("First workspace status should be RUNNING but is " + status, runningState, status);

        CheWorkspace secondWorkspace = provider.createCheWorkspace(null);
        CheWorkspaceService.waitUntilWorkspaceGetsToState(secondWorkspace, runningState, token);
        status = CheWorkspaceService.getWorkspaceStatus(secondWorkspace, token);
        Assert.assertEquals("Second workspace status should be RUNNING but is" + status, runningState, status);
        status = CheWorkspaceService.getWorkspaceStatus(secondWorkspace, token);
        Assert.assertEquals("First workspace status should be STOPPED but is " + status, stoppedState, status);
    }
}
