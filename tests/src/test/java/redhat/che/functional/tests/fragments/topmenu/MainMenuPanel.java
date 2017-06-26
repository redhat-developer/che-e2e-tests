package redhat.che.functional.tests.fragments.topmenu;

import static redhat.che.functional.tests.utils.ActionUtils.click;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * id = "gwt-debug-mainMenuPanel"
 */
public class MainMenuPanel {

    @Drone
    private WebDriver driver;

    @FindBy(id = "gwt-debug-MenuItem/profileGroup-true")
    private WebElement profileItem;

    @FindBy(id = "gwt-debug-MenuItem/git-true")
    private WebElement gitItem;

    public void clickProfile(){
        click(driver, profileItem);
    }

    public void clickGit(){
        click(driver, gitItem);
    }
}
