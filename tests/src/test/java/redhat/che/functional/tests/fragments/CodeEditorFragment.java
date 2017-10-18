package redhat.che.functional.tests.fragments;

import com.redhat.arquillian.che.CheWorkspaceManager;
import org.apache.log4j.Logger;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
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

    @Drone
    private WebDriver driver;

    private static int WAIT_TIME= 15;
    private WebElement label;

    private String dependencyToAdd =
        "<dependency>\n"
            + "<groupId>ch.qos.logback</groupId>\n"
            + "<artifactId>logback-core</artifactId>\n"
            + "<version>1.1.10</version>\n"
            + "</dependency>\n";


    public void writeDependencyIntoPom() {
        new Actions(driver).moveToElement(rootElement).sendKeys(dependencyToAdd).perform();
    }

    public boolean verifyAnnotationErrorIsPresent(){
        logger.info("Waiting for " + WAIT_TIME + " seconds until annotation error should be visible");
        waitGui().withTimeout(WAIT_TIME, TimeUnit.SECONDS).until(driver -> {
            if(annotationError == null) return false;
            annotationError.click();
            label = driver.findElement(By.className("tooltipTitle"));
            if (label.getText().contains("Package ch.qos.logback:logback-core-1.1.10 is vulnerable: CVE-2017-5929. Recommendation: use version ")) {
                logger.info("Annotation error is present.");
                return true;
            } else {
                return false;
            }
        });
        return true;
    }

    public void writeIntoTextViewContent(String text) {
        writeIntoElement(driver, lastSpan, text);
    }

    public void hideErrors() {
        rootElement.click();
        waitGui().until().element(label).is().not().visible();
    }

    public void deleteNextLines(int linesCount) {
        ActionUtils.markNextLines(linesCount, driver);
        ActionUtils.deleteMarkedLines(driver);
    }

    public void waitUnitlPomDependencyIsNotVisible() {
        waitGui().until().element(dependency).is().not().visible();
    }
}
