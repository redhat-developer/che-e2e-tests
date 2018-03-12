package redhat.che.functional.tests.fragments.topmenu;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import redhat.che.functional.tests.fragments.popup.PopupMenu;

public class ProfileTopMenu extends PopupMenu {

    @FindBy(id = "gwt-debug-topmenu/Profile/showPreferences")
    private WebElement preferencesItem;

    public void openPreferences(){
        click(preferencesItem);
    }
}
