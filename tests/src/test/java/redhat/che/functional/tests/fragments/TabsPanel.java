package redhat.che.functional.tests.fragments;

import com.google.common.base.Function;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.jboss.arquillian.graphene.Graphene.waitGui;
import static org.jboss.arquillian.graphene.Graphene.waitModel;

public class TabsPanel {

    @Root
    private WebElement rootElement;

    @FindByJQuery("div[focused] > div:last-child")
    private WebElement focusedTabClose;

    @FindByJQuery("div[focused]")
    private WebElement focusedTab;

    @FindByJQuery("div[unsaved]")
    private WebElement unsavedTab;

    public void waitUntilFocusedTabHasName(String tabName) {
        waitModel().until().element(rootElement).is().visible();
        waitModel().until((Function<WebDriver, Boolean>) webDriver -> focusedTab.getText().equals(tabName));
    }

    public void closeActiveTab(WebDriver driver) {
        focusedTabClose.click();
        try {
            driver.findElement(By.id("ask-dialog-ok")).click();
        } catch(Exception e){
            //if exception arise, changes were saved automatically
        }
    }

    public void waintUntilFocusedTabSaves() {
        waitGui().until().element(unsavedTab).is().not().visible();
    }
}
