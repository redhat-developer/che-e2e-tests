package redhat.che.functional.tests;

import com.redhat.arquillian.che.provider.CheWorkspaceProvider;
import com.redhat.arquillian.che.resource.CheWorkspace;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.*;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import redhat.che.functional.tests.fragments.CodeEditorFragment;
import redhat.che.functional.tests.fragments.topmenu.MainMenuPanel;
import redhat.che.functional.tests.fragments.window.AskForValueDialog;
import redhat.che.functional.tests.utils.ActionUtils;

@RunWith(Arquillian.class)
public class PomTestCase extends AbstractCheFunctionalTest {

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

    @FindBy(className = "currentLine")
    private WebElement currentLine;

    @Before
    public void importProject(){
        openBrowser();
    }

    @Test
    @InSequence(1)
    public void testPomXmlReference() {
        openPomXml();
        setCursorToLine(37);
        codeEditor.writeDependencyIntoPom();
        Assert.assertTrue("Annotation error is not visible.", codeEditor.verifyAnnotationErrorIsPresent());
    }

    private void setCursorToLine(int line) {
        ActionUtils.openMoveCursorDialog(driver);
        askForValueDialog.waitFormToOpen();
        askForValueDialog.typeAndWaitText(line);
        askForValueDialog.clickOkBtn();
        askForValueDialog.waitFormToClose();
    }

    private void openPomXml() {
        project.getResource("pom.xml").open();
        Graphene.waitGui().until().element(currentLine).is().visible();
    }
}
