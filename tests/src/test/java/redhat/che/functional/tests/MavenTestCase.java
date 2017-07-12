package redhat.che.functional.tests;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import redhat.che.functional.tests.fragments.CommandsManagerDialog;
import redhat.che.functional.tests.fragments.ToolbarDebugPanel;
import redhat.che.functional.tests.fragments.popup.DropDownMenu;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.jboss.arquillian.graphene.Graphene.waitGui;
import static org.jboss.arquillian.graphene.Graphene.waitModel;

/**
 * Created by katka on 22/06/17.
 */

@RunWith(Arquillian.class)
public class MavenTestCase extends AbstractCheFunctionalTest{
    @FindBy(id="gwt-debug-toolbarPanel")
    private ToolbarDebugPanel debugPanel;

    @FindByJQuery("pre:contains('Total time')")
    private WebElement consoleEnds;

    @FindByJQuery("pre:contains('BUILD SUCCESS')")
    private WebElement buildSuccess;

    @FindBy(id = "menu-lock-layer-id")
    private DropDownMenu dropDownMenu;

    @FindByJQuery("div#commandsManagerView")
    private CommandsManagerDialog commandsManagerDialog;

    private final String testName = "buildTest";
    private final String command = "cd ${current.project.path} && scl enable rh-maven33 'mvn clean install'";

    @Before
    public void setup(){
        openBrowser(driver);
        project.select();
    }

    /**
     * Tries to build project.
     */
    @Test
    @InSequence(1)
    public void test_maven_build() {
        //creating build command in top menu bar
        debugPanel.expandCommandsDropDown();
        dropDownMenu.selectEditCommand();
        commandsManagerDialog.addCustomCommand(testName, command);
        commandsManagerDialog.closeEditCommands();

        //running command (created command is automatically selected - no need to search for it in dropdown)
        debugPanel.executeCommand();

        //wait for end - if build first time, it last longer -> increasing timeout
        waitModel().withTimeout(2, TimeUnit.MINUTES).until().element(consoleEnds).is().visible();

        //find out if build was successful
        try {
            waitGui().withTimeout(1,TimeUnit.SECONDS).until().element(buildSuccess).is().visible();
        }catch(Exception e){
            Assert.fail("Project build failed!");
        }finally {
            //delete command
            debugPanel.expandCommandsDropDown();
            dropDownMenu.selectEditCommand();
            commandsManagerDialog.deleteCommand(testName);
        }

    }
}
