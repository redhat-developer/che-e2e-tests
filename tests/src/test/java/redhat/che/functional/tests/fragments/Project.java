package redhat.che.functional.tests.fragments;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * jquery = "#gwt-debug-projectTree > div[path='/" + PROJECT_NAME + "']"
 */
public class Project extends AbstractResource {

    @Drone
    private WebDriver driver;

    @Root
    private WebElement projectTreeRoot;

    @Override
    protected WebElement getResourceElement() {
        return projectTreeRoot;
    }

    /**
     * Gets underlying project item. If item is not visible, parent project items are expanded at first.
     * 
     * @param name visible in of a project item in the project explorer
     *            
     * @return project item
     */
    @SuppressWarnings("unchecked")
    public ProjectItem getResource(String name) {
        return new ProjectItem(driver, getChildResourceElement(name));
    }

    @Override
    public boolean resourceExists(String name) {
        try {
            getChildResourceElement(name);
            return true;
        } catch (NoSuchElementException ex) {
            return false;
        }
    }
    
    @Override
    protected WebDriver getWebDriver() {
        return driver;
    }
}
