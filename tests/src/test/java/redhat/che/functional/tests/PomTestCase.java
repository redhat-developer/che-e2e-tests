package redhat.che.functional.tests;

import org.apache.log4j.Logger;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.*;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import redhat.che.functional.tests.fragments.window.AskForValueDialog;
import redhat.che.functional.tests.utils.ActionUtils;

import static redhat.che.functional.tests.utils.Constants.XML;

@RunWith(Arquillian.class)
public class PomTestCase extends AbstractCheFunctionalTest {
    private static final Logger logger = Logger.getLogger(PomTestCase.class);

    @FindBy(id = "gwt-debug-askValueDialog-window")
    private AskForValueDialog askForValueDialog;

    @FindBy(className = "currentLine")
    private WebElement currentLine;

    private int line = 37;

    @Before
    public void importProject(){
        openBrowser();
    }

    @After
    public void deleteDependency(){
        editorPart.codeEditor().hideErrors();
        setCursorToLine(37);
        editorPart.codeEditor().deleteNextLines(5);
        editorPart.codeEditor().waitUnitlPomDependencyIsNotVisible();
        editorPart.tabsPanel().waintUntilFocusedTabSaves();
    }

    @Test
    public void testPomXmlReference() {
        openPomXml();
        setCursorToLine(37);
        codeEditor.writeDependency(XML);
        Assert.assertTrue("Annotation error is not visible.", codeEditor.verifyAnnotationErrorIsPresent(XML));
    }

    private void setCursorToLine(int line) {
        ActionUtils.openMoveCursorDialog(driver);
        askForValueDialog.waitFormToOpen();
        askForValueDialog.typeAndWaitText(line);
        askForValueDialog.clickOkBtn();
        askForValueDialog.waitFormToClose();
    }

    private void openPomXml() {
        vertxProject.getResource("pom.xml").open();
        Graphene.waitGui().until().element(currentLine).is().visible();
    }

}
