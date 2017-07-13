package redhat.che.functional.tests;

import static redhat.che.functional.tests.utils.Constants.TEST_FILE;

import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.support.FindBy;

import redhat.che.functional.tests.fragments.ProjectItem;
import redhat.che.functional.tests.fragments.TestResultsView;
import redhat.che.functional.tests.fragments.popup.ContextMenu;
import redhat.che.functional.tests.fragments.popup.Popup;

@RunWith(Arquillian.class)
public class JUnitTestExecutionTestCase extends AbstractCheFunctionalTest {

    @FindBy(id = "menu-lock-layer-id")
    private ContextMenu contextMenu;

    @FindByJQuery("div#gwt-debug-popup-container")
    private Popup popup;

    @FindByJQuery("div#gwt-debug-perspectivePanel")
    private TestResultsView testResultsView;

    @Test
    @InSequence(1)
    public void test_run_junit_test_and_verify_popup_window() {
        openBrowser();

        ProjectItem testFile = project.getResource("src").getResource("test").getResource("java").getResource("io/openshift/booster")
            .getResource(TEST_FILE);
        testFile.openContextMenu();

        contextMenu.selectRunJUnitClassTest();

        popup.waitUntilTestsAreRunnig();
        popup.waitWhileTestsAreRunning();
        popup.waitUntilTestsAreFinishedSuccessfully();

        testResultsView.open();
        testResultsView.waitUntilTestsPassed();
    }
}
