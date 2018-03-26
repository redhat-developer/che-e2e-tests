package redhat.che.functional.tests;

import com.redhat.arquillian.che.annotations.Workspace;
import com.redhat.arquillian.che.resource.Stack;
import org.apache.log4j.Logger;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.support.FindBy;
import redhat.che.functional.tests.fragments.BottomInfoPanel;
import redhat.che.functional.tests.fragments.popup.Popup;
import redhat.che.functional.tests.fragments.topmenu.GitPopupTopMenu;
import redhat.che.functional.tests.fragments.topmenu.ProfileTopMenu;
import redhat.che.functional.tests.fragments.window.CommitToRepoWindow;
import redhat.che.functional.tests.fragments.window.GitPushWindow;
import redhat.che.functional.tests.fragments.window.PreferencesWindow;
import java.util.Date;

@RunWith(Arquillian.class)
@Workspace(stackID = Stack.VERTX)
public class GitTestCase extends AbstractCheFunctionalTest {
	private static final Logger LOG = Logger.getLogger(GitTestCase.class);

    @FindByJQuery("table[title='Preferences']")
    private PreferencesWindow preferencesWindow;

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
    
    @FindBy(id = "gwt-debug-popup-container")
    private Popup popup;
    
	private void waitUntilProjectImported() {
		Graphene.waitGui().until().element(
				infoPanel.getNotificationManager().getNotificationElement("Project vertx-http-booster imported")).is()
				.present();
	}
	
    @Test
    @InSequence(1)
    public void test_load_ssh_key_and_set_commiter_information(){
        // set commiter credentials
        LOG.info("Starting: " + this.getClass().getName());
        openBrowser();
        waitUntilProjectImported();
        LOG.info("Test: test_load_ssh_key_and_set_commiter_information");
        mainMenuPanel.clickProfile();
        profileTopMenu.openPreferences();
        preferencesWindow.writeCommiterInformation("test-user", "test@mail.com");
        preferencesWindow.close();
    }

    @Test
    @InSequence(2)
    public void test_change_file_and_add_into_index() {
        //openBrowser(driver);

        vertxProject.getResource("README.md").open();
        editorPart.tabsPanel().waitUntilActiveTabHasName("README.md");
        editorPart.codeEditor().writeIntoElementContainingString("changes added on: " + new Date(), "changes added on:");

        mainMenuPanel.clickGit();
        gitPopupTopMenu.addToIndex();
        bottomInfoPanel.tabsPanel().waitUntilFocusedTabHasName(BottomInfoPanel.TabNames.TAB_GIT_ADD_TO_INDEX);
        bottomInfoPanel.waitUntilConsolePartContains(BottomInfoPanel.FixedConsoleText.GIT_ADDED_TO_INDEX_TEXT);
    }

    @Test
    @InSequence(3)
    public void test_create_commit() {
        //openBrowser(driver);

        mainMenuPanel.clickGit();
        gitPopupTopMenu.commitSelected();
        commitToRepoWindow.addCommitMessage("changed README as part of a test that was run on: " + new Date());
        commitToRepoWindow.commit();
        bottomInfoPanel.tabsPanel().waitUntilFocusedTabHasName(BottomInfoPanel.TabNames.TAB_GIT_COMMIT);
        bottomInfoPanel.waitUntilConsolePartContains(BottomInfoPanel.FixedConsoleText.GIT_COMMITED_WITH_REVISION);
    }

	/*
	 * This test case will only work, when it is run with osio user who has push
	 * access to github repo.
	 */
    
    @Test
    @InSequence(4)
    public void test_push_changes(){
        //openBrowser(driver);

        mainMenuPanel.clickGit();
        gitPopupTopMenu.push();
        gitPushWindow.push();
        popup.waitForPopup("Pushed to origin");
    }
}
