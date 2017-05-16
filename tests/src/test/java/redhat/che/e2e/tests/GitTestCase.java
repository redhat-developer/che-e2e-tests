package redhat.che.e2e.tests;

import java.io.File;
import java.util.Date;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;
import redhat.che.e2e.tests.fragments.BottomInfoPanel;
import redhat.che.e2e.tests.fragments.topmenu.GitPopupTopMenu;
import redhat.che.e2e.tests.fragments.topmenu.MainMenuPanel;
import redhat.che.e2e.tests.fragments.topmenu.ProfileTopMenu;
import redhat.che.e2e.tests.fragments.window.CommitToRepoWindow;
import redhat.che.e2e.tests.fragments.window.GitPushWindow;
import redhat.che.e2e.tests.fragments.window.PreferencesWindow;
import redhat.che.e2e.tests.fragments.window.UploadPrivateSshFormWindow;

import static redhat.che.e2e.tests.fragments.BottomInfoPanel.FixedConsoleText.GIT_ADDED_TO_INDEX_TEXT;
import static redhat.che.e2e.tests.fragments.BottomInfoPanel.FixedConsoleText.GIT_COMMITED_WITH_REVISION;
import static redhat.che.e2e.tests.fragments.BottomInfoPanel.TabNames.TAB_GIT_ADD_TO_INDEX;
import static redhat.che.e2e.tests.fragments.BottomInfoPanel.TabNames.TAB_GIT_COMMIT;

@RunWith(Arquillian.class)
public class GitTestCase extends AbstractCheEndToEndTest {

    @Drone
    private WebDriver driver;

    @FindByJQuery("div:contains('Preferences'):contains('Java Compiler'):last")
    private PreferencesWindow preferencesWindow;

    @FindByJQuery("div:contains('Host'):contains('Upload'):last")
    private UploadPrivateSshFormWindow uploadPrivateSshForm;

    @FindBy(id = "gwt-debug-mainMenuPanel")
    private MainMenuPanel mainMenuPanel;

    @FindBy(id = "menu-lock-layer-id")
    private ProfileTopMenu profileTopMenu;

    @FindBy(id = "menu-lock-layer-id")
    private GitPopupTopMenu gitPopupTopMenu;

    @FindBy(id = "gwt-debug-infoPanel")
    private BottomInfoPanel bottomInfoPanel;

    @FindBy(id = "gwt-debug-git-commit-window")
    private CommitToRepoWindow commitToRepoWindow;

    @FindBy(id = "gwt-debug-git-remotes-push-window")
    private GitPushWindow gitPushWindow;

    @Test
    @InSequence(1)
    public void test_load_ssh_key_and_set_commiter_information(){
        // set commiter credentials
        openBrowser(driver);

        mainMenuPanel.clickProfile();
        profileTopMenu.openPreferences();
        preferencesWindow.writeCommiterInformation("dev-test-user", "mlabuda@redhat.com");
        //preferencesWindow.openUploadPrivateKeyWindow();
        File idRsa = new File("src/test/resources/id_rsa");
        //uploadPrivateSshForm.upload("github.com", idRsa);
        preferencesWindow.close();
    }

    @Test
    @InSequence(2)
    public void test_change_file_and_add_into_index() {
        //openBrowser(driver);

        project.getResource("README.md").open();
        editorPart.tabsPanel().waitUntilFocusedTabHasName("README.md");
        editorPart.codeEditor().writeIntoTextViewContent("\n changes added on: " + new Date());

        mainMenuPanel.clickGit();
        gitPopupTopMenu.addToIndex();
        bottomInfoPanel.tabsPanel().waitUntilFocusedTabHasName(TAB_GIT_ADD_TO_INDEX);
        bottomInfoPanel.waitUntilConsolePartContains(GIT_ADDED_TO_INDEX_TEXT);
    }

    @Test
    @InSequence(3)
    public void test_create_commit() {
        //openBrowser(driver);

        mainMenuPanel.clickGit();
        gitPopupTopMenu.commitSelected();
        commitToRepoWindow.addCommitMessage("changed README as part of a test that was run on: " + new Date());
        commitToRepoWindow.commit();
        bottomInfoPanel.tabsPanel().waitUntilFocusedTabHasName(TAB_GIT_COMMIT);
        bottomInfoPanel.waitUntilConsolePartContains(GIT_COMMITED_WITH_REVISION);
    }

    @Test
    @InSequence(4)
    public void test_push_changes(){
        //openBrowser(driver);

        mainMenuPanel.clickGit();
        gitPopupTopMenu.push();
        gitPushWindow.push();
    }
}
