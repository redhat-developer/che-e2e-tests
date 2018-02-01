package redhat.che.functional.tests.fragments;

import com.redhat.arquillian.che.CheWorkspaceManager;
import com.redhat.arquillian.che.provider.CheWorkspaceProvider;
import org.apache.log4j.Logger;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import redhat.che.functional.tests.AbstractCheFunctionalTest;
import redhat.che.functional.tests.fragments.window.AskForValueDialog;
import redhat.che.functional.tests.utils.ActionUtils;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * id = "gwt-debug-editorPartStack-contentPanel"
 */
public class CodeEditorFragment {
    private static final Logger LOG = Logger.getLogger(CodeEditorFragment.class);

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

    private static final Integer WAIT_TIME = 15;

    public WebElement getRoot() {
        return rootElement;
    }

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

    public boolean verifyAnnotationErrorIsPresent(String expectedError, int editorLine) {
        LOG.info("Waiting for " + WAIT_TIME + " seconds until annotation error should be visible");
        try {
            Graphene.waitGui().withTimeout(WAIT_TIME, TimeUnit.SECONDS).until(webDriver -> {
                setCursorToLine(editorLine);
                new Actions(webDriver).moveToElement(annotationErrorEditorField).perform();
                Graphene.waitGui().until().element(annotationErrorToolTipIcon).is().visible();
                return annotationErrorToolTipText.getText().contains(expectedError);
            });
        } catch (WebDriverException e){
            LOG.error("Annotation assert failed:"+e.getMessage());
            return false;
        }
        return true;
    }

    public void writeIntoTextViewContent(String text) {
        ActionUtils.writeIntoElement(driver, lastSpan, text);
    }

    public void hideErrors(Integer annotationLine) {
        try {
            setCursorToLine(annotationLine);
            annotationErrorEditorField.click();
            Graphene.waitGui().until().element(annotationErrorToolTip).is().not().visible();
        } catch (WebDriverException e) {
            if (AbstractCheFunctionalTest.isProdPreview()) {
                LOG.info("Expected error encountered:" + AbstractCheFunctionalTest.bayesianErrorNotVisible);
            } else {
                LOG.error("Annotation assert failed:" + e.getMessage());
                throw e;
            }
        }
    }

    public void deleteNextLines(int linesCount) {
        ActionUtils.markNextLines(linesCount, driver);
        ActionUtils.deleteMarkedLines(driver);
    }

    public void waitUnitlPomDependencyIsNotVisible() {
        Graphene.waitGui().until().element(dependency).is().not().visible();
    }
}
