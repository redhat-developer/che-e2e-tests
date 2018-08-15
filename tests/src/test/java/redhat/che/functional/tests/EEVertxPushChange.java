package redhat.che.functional.tests;

import com.redhat.arquillian.che.annotations.Workspace;
import com.redhat.arquillian.che.resource.Stack;
import org.apache.log4j.Logger;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import redhat.che.functional.tests.fragments.BottomInfoPanel;
import redhat.che.functional.tests.fragments.popup.Popup;
import redhat.che.functional.tests.fragments.topmenu.GitPopupTopMenu;
import redhat.che.functional.tests.fragments.topmenu.ProfileTopMenu;
import redhat.che.functional.tests.fragments.window.CommitToRepoWindow;
import redhat.che.functional.tests.fragments.window.GitPushWindow;
import redhat.che.functional.tests.fragments.window.PreferencesWindow;
import redhat.che.functional.tests.utils.ActionUtils;

import java.util.Date;

@RunWith(Arquillian.class)
@Workspace(stackID = Stack.VERTX)
public class EEVertxPushChange extends AbstractCheFunctionalTest {
	private static final Logger LOG = Logger.getLogger(EEVertxPushChange.class);

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

    @FindBy(id = "gwt-debug-navigateToFile-fileName")
    private WebElement findClassDialogInput;

    @FindByJQuery("#gwt-debug-navigateToFile-suggestionPanel tr[selected='SELECTED'")
    private WebElement selectedItem;

    @FindByJQuery("#gwt-debug-git-commit-window-windowFrameButtonBar option:contains('origin/master')")
    private WebElement masterBranch;

    private void openClass(String fileName, String extension){
        ActionUtils.findClassDialog(driver);
        ActionUtils.writeIntoElement(driver, findClassDialogInput, fileName+extension);
        Graphene.waitGui().until().element(selectedItem).is().visible();
        ActionUtils.pressEnter(driver);
    }

    @Test
    @InSequence(1)
    public void change_hello_to_bonjour(){
        LOG.info("Starting: " + this.getClass().getName());
        openBrowser();
        waitUntilProjectIsResolved();
        String fileName = "HttpApplication", extension = ".java";
        openClass(fileName, extension);
        editorPart.tabsPanel().waitUntilActiveTabHasName(fileName);
        editorPart.codeEditor().writeIntoElementContainingString("protected static final String template = \"Bonjour, %s!\";", "Hello");
        editorPart.tabsPanel().waintUntilFocusedTabSaves();
        editorPart.tabsPanel().closeActiveTab(driver);

        mainMenuPanel.clickGit();
        gitPopupTopMenu.addToIndex();
	}

    @Test
    @InSequence(2)
    public void commit_and_push() {
        ActionUtils.openGitCommit(driver);
        commitToRepoWindow.addCommitMessage("Hello changed to Bonjour on " + new Date());
        commitToRepoWindow.checkPushCheckbox();
        masterBranch.click();
        commitToRepoWindow.commit();
        bottomInfoPanel.tabsPanel().waitUntilFocusedTabHasName(BottomInfoPanel.TabNames.TAB_GIT_COMMIT);
        bottomInfoPanel.waitUntilConsolePartContains(BottomInfoPanel.FixedConsoleText.GIT_COMMITED_WITH_REVISION);
        popup.waitForPopup("Pushed to origin");
    }
}