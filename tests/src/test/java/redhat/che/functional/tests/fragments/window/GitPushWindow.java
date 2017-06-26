package redhat.che.functional.tests.fragments.window;

import static redhat.che.functional.tests.utils.ActionUtils.click;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * id = "gwt-debug-git-remotes-push-window"
 */
public class GitPushWindow {

    @Drone
    private WebDriver driver;

    @FindBy(id = "git-remotes-push-push")
    private WebElement pushButton;

    public void push(){
        click(driver, pushButton);
    }

}
