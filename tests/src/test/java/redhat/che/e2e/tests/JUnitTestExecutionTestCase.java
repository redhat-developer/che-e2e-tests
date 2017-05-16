package redhat.che.e2e.tests;

import static redhat.che.e2e.tests.utils.Constants.TEST_FILE;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import redhat.che.e2e.tests.fragments.ContextMenu;
import redhat.che.e2e.tests.fragments.Popup;
import redhat.che.e2e.tests.fragments.ProjectItem;
import redhat.che.e2e.tests.fragments.TestResultsView;

@RunWith(Arquillian.class)
public class JUnitTestExecutionTestCase extends AbstractCheEndToEndTest {

    @Drone
    private WebDriver driver;

    @FindBy(id = "menu-lock-layer-id")
    private ContextMenu contextMenu;

    @FindByJQuery("div#gwt-debug-popup-container")
    private Popup popup;

    @FindByJQuery("div#gwt-debug-perspectivePanel")
    private TestResultsView testResultsView;

    @Test
    @InSequence(1)
    public void test_run_junit_test_and_verify_popup_window() {
        openBrowser(driver);

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
