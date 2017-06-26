package redhat.che.functional.tests.fragments.window;

import java.io.File;
import java.util.List;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.jboss.arquillian.graphene.Graphene.waitAjax;

/**
 * jquery = "div:contains('Host'):contains('Upload'):last"
 */
public class UploadPrivateSshFormWindow {

    @Root
    private WebElement formRootElement;

    @FindBy(tagName = "input")
    private List<WebElement> inputs;

    @FindBy(id = "sshKeys-upload")
    private WebElement uploadButton;

    public void upload(String host, File keyFile){
        waitAjax().until().element(formRootElement).is().visible();
        inputs.get(0).sendKeys(host);
        inputs.get(1).sendKeys(keyFile.getAbsolutePath());

        waitAjax().until().element(uploadButton).is().enabled();
        uploadButton.click();
    }
}
