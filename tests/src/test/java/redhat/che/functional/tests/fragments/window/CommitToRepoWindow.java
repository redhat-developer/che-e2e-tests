package redhat.che.functional.tests.fragments.window;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import redhat.che.functional.tests.utils.ActionUtils;

/**
 * id = "gwt-debug-git-commit-window"
 */
public class CommitToRepoWindow {

    @Drone
    private WebDriver browser;

    @FindBy(id = "gwt-debug-git-commit-message")
    private WebElement commitMessageArea;

    @FindBy(id = "git-commit-commit")
    private WebElement commitButton;

    @FindBy(id = "gwt-debug-push-after-commit-check-box-label")
    private WebElement pushCheckbox;


    public void addCommitMessage(String message){
        ActionUtils.writeIntoElement(browser, commitMessageArea, message);
    }

    public void commit(){
        commitButton.click();
    }

    public void checkPushCheckbox() {
        pushCheckbox.click();
    }
}
