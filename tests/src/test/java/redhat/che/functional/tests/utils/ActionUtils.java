package redhat.che.functional.tests.utils;

import static org.jboss.arquillian.graphene.Graphene.waitAjax;
import static org.jboss.arquillian.graphene.Graphene.waitGui;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class ActionUtils {
	
	private static final Logger LOG = Logger.getLogger(ActionUtils.class);

    public static void writeIntoElement(WebDriver driver, WebElement element, String text) {
        waitAjax().until().element(element).is().present();
        new Actions(driver).moveToElement(element).click().sendKeys(text).perform();
    }

    public static void click(WebDriver driver,WebElement element) {
        waitGui().until().element(element).is().visible();
        waitGui().withTimeout(10, TimeUnit.SECONDS).withMessage("Trying to click on "+element+" failed").until(d->{
        	try {
        		element.click();
        		return true;
        	}catch (WebDriverException ex) {
        		LOG.debug("Element is not clickable");
        		return false;
        	}
        });
    }

    public static void openMoveCursorDialog(WebDriver driver) {
        new Actions(driver)
                .sendKeys(Keys.chord(Keys.CONTROL, "l"))
                .perform();
    }

    public static void markNextLines(int linesCount, WebDriver driver){
        for (int i=0; i < linesCount; i++){
            new Actions(driver)
                    .sendKeys(Keys.chord(Keys.SHIFT, Keys.ARROW_DOWN))
                    .perform();
        }
    }

    public static void deleteMarkedLines(WebDriver driver) {
        new Actions(driver).sendKeys(Keys.DELETE).perform();
    }

    public static void doubleClick(WebDriver driver, WebElement element){
        new Actions(driver).doubleClick(element).perform();
    }

    public static void selectAll(WebDriver driver){
        new Actions(driver)
                .sendKeys(Keys.chord(Keys.CONTROL, "a"))
                .perform();

    }

    public static void moveCursorToElement(WebDriver driver, WebElement element){
        new Actions(driver).moveToElement(element).perform();
    }

    public static void findClassDialog(WebDriver driver){
        new Actions(driver).sendKeys(Keys.chord(Keys.CONTROL, Keys.ALT, "n")).perform();
    }

    public static void openGitCommit(WebDriver driver) {
        new Actions(driver).sendKeys(Keys.chord(Keys.ALT, "c")).perform();
    }

    public static void pressEnter(WebDriver driver){
        new Actions(driver).sendKeys(Keys.ENTER).perform();
    }

}
