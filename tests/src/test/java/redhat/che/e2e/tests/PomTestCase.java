package redhat.che.e2e.tests;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import redhat.che.e2e.tests.fragments.CodeEditorFragment;

@RunWith(Arquillian.class)
public class PomTestCase extends AbstractCheEndToEndTest {

    @Drone
    private WebDriver browser;

    @FindBy(id = "gwt-debug-editorPartStack-contentPanel")
    private CodeEditorFragment codeEditor;

    @Test
    public void test_when_pom_has_older_dependency_version_annotation_error_is_shown() {
        openBrowser(browser);
        project.getResource("pom.xml").open();
        codeEditor.writeDependencyIntoPom();
        codeEditor.verifyAnnotationErrorIsPresent();
    }
}
