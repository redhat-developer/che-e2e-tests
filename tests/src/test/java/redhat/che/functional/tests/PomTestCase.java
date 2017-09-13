//package redhat.che.functional.tests;
//
//import static org.junit.Assert.*;
//
//import org.jboss.arquillian.junit.Arquillian;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.openqa.selenium.support.FindBy;
//
//import redhat.che.functional.tests.fragments.CodeEditorFragment;
//
//@RunWith(Arquillian.class)
//public class PomTestCase extends AbstractCheFunctionalTest {
//
//    @FindBy(id = "gwt-debug-editorPartStack-contentPanel")
//    private CodeEditorFragment codeEditor;
//
//    @Test
//    public void test_when_pom_has_older_dependency_version_annotation_error_is_shown() {
//        openBrowser();
//        project.getResource("pom.xml").open();
//        codeEditor.writeDependencyIntoPom();
//        codeEditor.verifyAnnotationErrorIsPresent();
//        fail();
//    }
//}
