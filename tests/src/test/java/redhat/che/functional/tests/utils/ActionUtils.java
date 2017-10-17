package redhat.che.functional.tests.utils;

import static org.jboss.arquillian.graphene.Graphene.waitAjax;
import static org.jboss.arquillian.graphene.Graphene.waitGui;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ActionUtils {

    public static void writeIntoElement(WebDriver driver, WebElement element, String text) {
        waitAjax().until().element(element).is().present();
        new Actions(driver).moveToElement(element).click().sendKeys(text).perform();
    }

    public static void click(WebDriver driver,WebElement element) {
        waitGui().until().element(element).is().visible();
        new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(element));
        element.click();
    }

    public static void openMoveCursorDialog(WebDriver driver) {
        new Actions(driver)
                .sendKeys(Keys.chord(Keys.CONTROL, "l"))
                .perform();
    }
}
