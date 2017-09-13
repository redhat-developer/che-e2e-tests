package redhat.che.functional.tests.fragments.topmenu;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import redhat.che.functional.tests.fragments.popup.PopupMenu;

public class GitPopupTopMenu extends PopupMenu {

    @FindBy(id = "gwt-debug-topmenu/Git/gitAddToIndex")
    private WebElement addToIndexItem;

    @FindBy(id = "gwt-debug-topmenu/Git/gitRemoteGroup")
    private WebElement remoteGroupItem;

    @FindBy(id = "gwt-debug-topmenu/Git/Remotes.../gitPush")
    private WebElement pushItem;

    @FindBy(id = "topmenu/Git/Commit ...")
    private WebElement commitItem;

    public void addToIndex(){
        click(addToIndexItem);
    }

    public void push(){
        click(remoteGroupItem);
        click(pushItem);
    }

    public void commitSelected(){
        click(commitItem);
    }
}
