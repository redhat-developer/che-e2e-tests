package redhat.che.functional.tests;

import com.redhat.arquillian.che.CheWorkspaceManager;
import com.redhat.arquillian.che.config.CheExtensionConfiguration;
import com.redhat.arquillian.che.provider.CheWorkspaceProvider;
import com.redhat.arquillian.che.resource.CheWorkspace;
import com.redhat.arquillian.che.resource.CheWorkspaceStatus;
import com.redhat.arquillian.che.service.CheWorkspaceService;
import org.apache.log4j.Logger;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.*;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import redhat.che.functional.tests.fragments.CodeEditorFragment;
import redhat.che.functional.tests.fragments.window.AskForValueDialog;
import redhat.che.functional.tests.utils.ActionUtils;

import java.util.concurrent.TimeUnit;

import static com.redhat.arquillian.che.util.Constants.CREATE_WORKSPACE_REQUEST_NODEJS_JSON;
import static redhat.che.functional.tests.utils.Constants.JSON;

@RunWith(Arquillian.class)
public class PackageJsonTestCase extends AbstractCheFunctionalTest {
    private static final Logger logger = Logger.getLogger(CheWorkspaceManager.class);

    @ArquillianResource
    private static CheWorkspace firstWorkspace;

    @ArquillianResource
    private static CheWorkspaceProvider provider;

    @FindBy(id = "gwt-debug-editorPartStack-contentPanel")
    private CodeEditorFragment codeEditor;

    @FindBy(id = "gwt-debug-askValueDialog-window")
    private AskForValueDialog askForValueDialog;

    @FindBy(id = "gwt-debug-askValueDialog-window")
    private WebElement valueDialog;

    @FindByJQuery("div:contains('Initializing')")
    private WebElement initializingDialog;

    CheWorkspace nodejsWorkspace;
    String token;

    @FindBy(className = "currentLine")
    private WebElement currentLine;

    @Before
    public void setEnvironment(){
        nodejsWorkspace = provider.createCheWorkspace(CREATE_WORKSPACE_REQUEST_NODEJS_JSON);
        logger.info("Workspace with node.js project created.");
        token = "Bearer " + System.getProperty(CheExtensionConfiguration.KEYCLOAK_TOKEN_PROPERTY_NAME);
        String runningState = CheWorkspaceStatus.RUNNING.getStatus();
        CheWorkspaceService.waitUntilWorkspaceGetsToState(nodejsWorkspace, runningState, token);

        openBrowser(nodejsWorkspace);
    }

    @After
    public void resetEnvironment(){
        Assert.assertTrue(provider.stopWorkspace(nodejsWorkspace));
        Assert.assertTrue(provider.startWorkspace(firstWorkspace));
        CheWorkspaceService.deleteWorkspace(nodejsWorkspace, token);
    }

    @Test(expected = org.openqa.selenium.TimeoutException.class)
    public void testPackageJsonBayesian(){
        openPackageJson();
        setCursorToLine(12);
        codeEditor.writeDependency(JSON);
        Assert.assertTrue("Annotation error is not visible.", codeEditor.verifyAnnotationErrorIsPresent(JSON));
    }

    private void openPackageJson() {
        nodejsProject.getResource("package.json").open();
        Graphene.waitGui().withTimeout(90, TimeUnit.SECONDS).until().element(currentLine).is().visible();
    }

    private void setCursorToLine(int line) {
        ActionUtils.openMoveCursorDialog(driver);
        askForValueDialog.waitFormToOpen();
        askForValueDialog.typeAndWaitText(line);
        askForValueDialog.clickOkBtn();
        askForValueDialog.waitFormToClose();
    }
}
