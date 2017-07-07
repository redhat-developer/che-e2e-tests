package redhat.che.functional.tests;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.jboss.arquillian.graphene.Graphene.waitGui;
import static org.jboss.arquillian.graphene.Graphene.waitModel;

/**
 * Created by katka on 22/06/17.
 */

@RunWith(Arquillian.class)
public class MavenTestCase extends AbstractCheFunctionalTest{
    @Drone
    private WebDriver driver;

    @FindBy(id="CommandsGroup/build")
    private WebElement build;

    @FindBy(id = "CommandsGroup/Edit Commands...")
    private WebElement editCommands;

    @FindByJQuery("#gwt-debug-categoryHeader-custom > span > span > span:gt(1)")
    private WebElement customPlus;

    @FindBy(id = "gwt-debug-arbitraryPageView-cmdLine")
    private WebElement commandInput;

    @FindBy(id = "window-edit-commands-save")
    private WebElement saveButton;

    @FindBy(id = "window-edit-commands-close")
    private WebElement closeButton;

    @FindByJQuery(".GJHBXB5BHDB tr[id*='" + testName + "'] td:gt(0)")
    private WebElement buildOption;

    @FindBy(id="gwt-debug-ActionButton/executeSelectedCommand-true")
    private WebElement runCommand;

    @FindByJQuery("input:text[class=gwt-TextBox]")
    private WebElement nameInput;

    @FindBy(id = "gwt-debug-projectTree")
    private WebElement project;

    @FindByJQuery("div[id='gwt-debug-categoryHeader-custom'] ~ div > div:contains('" + testName + "')")
    private WebElement rowToDelete;

    @FindByJQuery("div[id='gwt-debug-categoryHeader-custom'] ~ div > div:contains('" + testName + "') > span > span")
    private WebElement rowToDeleteMinus;

    @FindBy(id = "ask-dialog-ok")
    private WebElement okButton;

    @FindByJQuery("pre:contains('Total time')")
    private WebElement consoleEnds;

    @FindByJQuery("pre:contains('BUILD SUCCESS')")
    private WebElement buildSuccess;

    @FindByJQuery("div:contains('Deleting command')")
    private WebElement deletingLoader;

    private final String testName = "buildTest";

    @Test
    @InSequence(1)
    public void test_maven_build() {
        openBrowser(driver);

        //creating focus on project
        select(project);

        //creating build command in top menu bar
        List<WebElement> dropdowns = driver.findElements(By.id("gwt-debug-dropDownHeader"));
        select(dropdowns.get(1));
        select(editCommands);
        select(customPlus);
        //setting variables
        select(commandInput);
        commandInput.clear();
        commandInput.sendKeys("cd ${current.project.path} && scl enable rh-maven33 'mvn clean install'");
        nameInput.clear();
        nameInput.sendKeys(testName);

        saveButton.click();
        waitGui().until().element(saveButton).is().not().enabled();
        closeButton.click();
        waitGui().until().element(closeButton).is().not().visible();

        //running command (created command is automatically selected)
        select(runCommand);

        //wait for end - if build first time, it last longer - increasing timeout
        waitModel().withTimeout(2, TimeUnit.MINUTES);
        waitModel().until().element(consoleEnds).is().visible();
        //set back to default value
        waitModel().withTimeout(5, TimeUnit.SECONDS);

        try {
            waitGui().until().element(buildSuccess).is().visible();
        }catch(Exception e){
            Assert.fail("Project build failed!");
        }
        //delete command
        select(dropdowns.get(1));

        select(editCommands);
        select(rowToDelete);
        select(rowToDeleteMinus);
        select(okButton);
        waitModel().until().element(closeButton).is().visible();

        //waiting for deleting command
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

    }

    private void select(WebElement element) {
        waitModel().until().element(element).is().visible();
        new Actions(driver).click(element).build().perform();
    }
}
