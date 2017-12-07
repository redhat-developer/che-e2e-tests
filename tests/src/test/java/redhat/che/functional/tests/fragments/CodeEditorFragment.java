package redhat.che.functional.tests.fragments;

import com.redhat.arquillian.che.CheWorkspaceManager;
import org.apache.log4j.Logger;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import redhat.che.functional.tests.utils.ActionUtils;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static org.jboss.arquillian.graphene.Graphene.waitGui;
import static redhat.che.functional.tests.utils.ActionUtils.writeIntoElement;

/**
 * id = "gwt-debug-editorPartStack-contentPanel"
 */
public class CodeEditorFragment {
    private static final Logger logger = Logger.getLogger(CheWorkspaceManager.class);

    @Root
    private WebElement rootElement;

    @FindByJQuery("div.annotation.error > div.annotationHTML.error")
    private List<WebElement> annotationErrors;

    @FindBy(className = "tooltipTitle")
    private WebElement annotationErrorSpan;

    @FindByJQuery("span:last")
    private WebElement lastSpan;

    @FindByJQuery("div:contains('ch.qos.logback')")
    private WebElement dependency;

    @Drone
    private WebDriver driver;

    private static int WAIT_TIME = 15;
    private WebElement label;

    public void writeDependency(String dependency) {
        new Actions(driver).moveToElement(rootElement).sendKeys(dependency).perform();
    }

    public boolean verifyAnnotationErrorIsPresent(String expectedError) {
        logger.info("Waiting for " + WAIT_TIME + " seconds until annotation error should be visible");
        try {
            waitGui().withTimeout(WAIT_TIME, TimeUnit.SECONDS).until(driver -> {

                for(WebElement error : annotationErrors){
                    error.click();

                    label = driver.findElement(By.className("tooltipTitle"));
                    if (label.getText().contains(expectedError)) {
                        logger.info("Annotation error is present.");
                        return true;
                    }
                }
                return false;
                });
        } catch (IndexOutOfBoundsException | WebDriverException i){
            return false;
        }
        return true;
    }

    public void writeIntoTextViewContent(String text) {
        writeIntoElement(driver, lastSpan, text);
    }

    public void hideErrors() {
        rootElement.click();
        waitGui().until().element(label).is().not().visible();
        try {
        	// Sometimes, the tooltip pops up once again. Get rid of it again.
        	waitGui().withTimeout(1, TimeUnit.SECONDS).until().element(By.className("tooltipTitle")).is().visible();
        	rootElement.click();
        }catch (TimeoutException e) {
        	// Tooltip was successfully hidden. Do nothing.
        }
    }

    public void deleteNextLines(int linesCount) {
        ActionUtils.markNextLines(linesCount, driver);
        ActionUtils.deleteMarkedLines(driver);
    }

    public void waitUnitlPomDependencyIsNotVisible() {
        waitGui().until().element(dependency).is().not().visible();
    }
}
