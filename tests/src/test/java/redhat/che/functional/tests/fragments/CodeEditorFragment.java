package redhat.che.functional.tests.fragments;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.jboss.arquillian.graphene.Graphene.waitGui;
import static redhat.che.functional.tests.utils.ActionUtils.writeIntoElement;

/**
 * id = "gwt-debug-editorPartStack-contentPanel"
 */
public class CodeEditorFragment {

    @Root
    private WebElement rootElement;

    @FindByJQuery("div:contains('vertx-core') > span:last")
    private WebElement emptyElementAfterVertxDep;

    @FindByJQuery("div.annotation.error > div.annotationHTML.error:last")
    private WebElement annotationError;

    private String dependencyToAdd =
        "\n</dependency> \n"
            + "<dependency>\n"
            + "<groupId>ch.qos.logback</groupId>\n"
            + "<artifactId>logback-core</artifactId>\n"
            + "<version>1.1.10</version>";

    @FindByJQuery("span:last")
    private WebElement lastSpan;

    @Drone
    private WebDriver browser;

    public void writeDependencyIntoPom() {
        writeIntoElement(browser, emptyElementAfterVertxDep, dependencyToAdd);
    }

    public void verifyAnnotationErrorIsPresent(){
        waitGui()
            .withMessage("The annotation error should be visible")
            .until()
            .element(annotationError)
            .is()
            .present();
    }

    public void writeIntoTextViewContent(String text) {
        writeIntoElement(browser, lastSpan, text);
    }
}
