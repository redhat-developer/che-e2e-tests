package redhat.che.functional.tests.fragments.window;

import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.jboss.arquillian.graphene.Graphene.waitAjax;

/**
 * jquery = "div:contains('Preferences'):contains('Java Compiler'):last"
 */
public class PreferencesWindow {

    @Root
    private WebElement preferencesWindowElement;

    @FindBy(id = "gwt-debug-projectWizard-Committer")
    private WebElement gitCommiterItem;

    @FindBy(id = "gwt-debug-committer-preferences-name")
    private WebElement gitCommiterNameInput;

    @FindBy(id = "gwt-debug-committer-preferences-email")
    private WebElement gitCommiterEmailInput;

    @FindBy(id = "window-preferences-storeChanges")
    private WebElement saveButton;

    @FindBy(id = "window-preferences-close")
    private WebElement closeButton;

    public void writeCommiterInformation(String name, String email){
        waitAjax().until().element(gitCommiterItem).is().visible();
        gitCommiterItem.click();
        waitAjax().until().element(gitCommiterNameInput).is().visible();
        gitCommiterNameInput.clear();
        gitCommiterNameInput.sendKeys(name);
        gitCommiterEmailInput.clear();
        gitCommiterEmailInput.sendKeys(email);
        saveButton.click();
    }

    public void close(){
        closeButton.click();
        waitAjax().until().element(preferencesWindowElement).is().not().visible();
    }
}
