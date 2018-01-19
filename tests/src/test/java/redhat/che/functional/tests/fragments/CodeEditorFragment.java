package redhat.che.functional.tests.fragments;

import com.redhat.arquillian.che.CheWorkspaceManager;
import org.apache.log4j.Logger;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import redhat.che.functional.tests.fragments.window.AskForValueDialog;
import redhat.che.functional.tests.utils.ActionUtils;

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

    @FindByJQuery("div.annotation.error > div.annotationHTML.error:last")
    private WebElement annotationError;

    @FindBy(className = "tooltipTitle")
    private WebElement annotationErrorSpan;

    @FindByJQuery("span:last")
    private WebElement lastSpan;

    @FindByJQuery("div:contains('ch.qos.logback')")
    private WebElement dependency;

    @FindByJQuery("body .textviewTooltip")
    private WebElement annotationErrorToolTip;

    @FindByJQuery("body .textviewTooltip .tooltipRow .annotationHTML.error")
    private WebElement annotationErrorToolTipIcon;

    @FindByJQuery("body .textviewTooltip .tooltipRow .tooltipTitle")
    private WebElement annotationErrorToolTipText;

    @FindByJQuery("body .textviewContent[contenteditable='true'] .annotationLine.currentLine .annotationRange.error")
    private WebElement annotationErrorEditorField;

    @FindByJQuery("body #gwt-debug-askValueDialog-window")
    private AskForValueDialog askForValueDialog;
    @Drone
    private WebDriver driver;

    private static int WAIT_TIME = 15;
    private WebElement label;
    private static final String EXPECTED_ERROR = "Package ch.qos.logback:logback-core-1.1.10 is vulnerable: CVE-2017-5929. Recommendation: use version 1.2.1";

    public void writeDependency(String dependency) {
        new Actions(driver).moveToElement(rootElement).sendKeys(dependency).perform();
    }

    public void setCursorToLine(int line) {
        ActionUtils.openMoveCursorDialog(driver);
        askForValueDialog.waitFormToOpen();
        askForValueDialog.typeAndWaitText(line);
        askForValueDialog.clickOkBtn();
        askForValueDialog.waitFormToClose();
    }

    public boolean verifyAnnotationErrorIsPresent(String expectedError) {
        logger.info("Waiting for " + WAIT_TIME + " seconds until annotation error should be visible");
        try {
            waitGui().withTimeout(WAIT_TIME, TimeUnit.SECONDS).until(webDriver -> {
                setCursorToLine(40);
                new Actions(webDriver).moveToElement(annotationErrorEditorField).perform();
                try {
                    waitGui().until().element(annotationErrorToolTipIcon).is().visible();
                } catch (WebDriverException e) {
                    return false;
                }
                return annotationErrorToolTipText.getText().equals(EXPECTED_ERROR);
//                if (annotationError == null) return false;
//                annotationError.click();
//                label = driver.findElement(By.className("tooltipTitle"));
//                if (label.getText().contains(expectedError)) {
//                    logger.info("Annotation error is present.");
//                    return true;
//                }
            });
        } catch (TimeoutException e){
            return false;
        }
        return true;
    }

    public void writeIntoTextViewContent(String text) {
        writeIntoElement(driver, lastSpan, text);
    }

    public void hideErrors() {
        annotationErrorEditorField.click();
        waitGui().until().element(annotationErrorToolTip).is().not().visible();
//        rootElement.click();
//        new Actions(driver).moveToElement(rootElement).perform();
        /*try { // this should be an error
        	// Sometimes, the tooltip pops up once again. Get rid of it again.
        	waitGui().withTimeout(1, TimeUnit.SECONDS).until().element(By.className("tooltipTitle")).is().visible();
        	rootElement.click();
        }catch (TimeoutException e) {
        	// Tooltip was successfully hidden. Do nothing.
        }*/
    }

    public void deleteNextLines(int linesCount) {
        ActionUtils.markNextLines(linesCount, driver);
        ActionUtils.deleteMarkedLines(driver);
    }

    public void waitUnitlPomDependencyIsNotVisible() {
        waitGui().until().element(dependency).is().not().visible();
    }
}
