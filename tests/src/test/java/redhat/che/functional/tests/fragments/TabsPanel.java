package redhat.che.functional.tests.fragments;

import com.google.common.base.Function;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.jboss.arquillian.graphene.Graphene.waitModel;

public class TabsPanel {

    @Root
    private WebElement rootElement;

    @FindByJQuery("div[focused]")
    private WebElement focusedTab;

    public void waitUntilFocusedTabHasName(String tabName) {
        waitModel().until().element(rootElement).is().visible();
        waitModel().until((Function<WebDriver, Boolean>) webDriver -> focusedTab.getText().equals(tabName));
    }
}
