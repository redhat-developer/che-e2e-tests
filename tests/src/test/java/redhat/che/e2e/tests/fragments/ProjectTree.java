package redhat.che.e2e.tests.fragments;

import java.util.List;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import static org.jboss.arquillian.graphene.Graphene.waitModel;
import static redhat.che.e2e.tests.utils.Constants.PROJECT_NAME;

/**
 * jquery = "#gwt-debug-projectTree > div:contains('" + PROJECT_NAME +
 * "'):first"
 */
public class ProjectTree {

    @Drone
    private WebDriver driver;

    @Root
    private WebElement projectTreeRoot;

    @FindBy(id = "projectFolder")
    private WebElement rootProjectFolder;

    @FindBy(id = "simpleFolder")
    private List<WebElement> subFolders;

    @FindByJQuery("div:contains('pom.xml'):last")
    private WebElement pomXmlItem;

    @FindByJQuery("div:contains('README.md'):last")
    private WebElement readmeMdItem;

    protected void setRootElement(WebElement root) {
        this.projectTreeRoot = root;
    }

    protected void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    public void expandProjectRoot() {
        setDefaultRoot();
        waitModel().until().element(projectTreeRoot).is().visible();
        if (subFolders.size() == 0) {
            new Actions(driver).doubleClick(rootProjectFolder).build().perform();
        }
    }

    /**
     * Opens a visible folder and gets this project tree. Usable to call several
     * openFolder methods in sequence.
     * 
     * @param name
     *            name of a folder. Maven project structure usually have only
     *            last folder, e.g. com.redhat.che.e2e would have name equals to
     *            e2e
     * @return this project tree
     */
    public ProjectTree openFolder(String name) {
        ByJQuery byJQuery = ByJQuery.selector("div[name=\"" + name + "\"]");
        waitModel().until().element(byJQuery).is().visible();
        new Actions(driver).doubleClick(driver.findElement(byJQuery)).build().perform();
        // When going in hierarchy, I have to update place where to start from
        rootProjectFolder = driver.findElement(byJQuery);
        return this;
    }

    /**
     * Selects a visible file.
     * 
     * @param name
     *            name of a file to select.
     */
    public void selectFile(String name) {
        setDefaultRoot();
        ByJQuery byJQuery = ByJQuery.selector("div[name=\"" + name + "\"]");
        waitModel().until().element(byJQuery).is().visible();
        new Actions(driver).click(driver.findElement(byJQuery)).build().perform();
    }

    private void open(WebElement item) {
        waitModel().until().element(item).is().visible();
        new Actions(driver).doubleClick(item).perform();
    }

    /**
     * Opens context menu on a file with specified name.
     * 
     * @param fileName
     *            to open its context menu
     */
    public void openContextMenu(String fileName) {
        selectFile(fileName);
        ByJQuery byJQuery = ByJQuery.selector("div[name=\"" + fileName + "\"]");
        new Actions(driver).contextClick(driver.findElement(byJQuery)).build().perform();
    }

    private void setDefaultRoot() {
        ByJQuery selector = ByJQuery.selector("#gwt-debug-projectTree > div:contains('" + PROJECT_NAME + "'):first");
        waitModel().until().element(selector).is().visible();
        rootProjectFolder = driver.findElement(selector);
    }

    /**
     * Opens pom.xml file
     */
    public void openPomXml() {
        setDefaultRoot();
        open(pomXmlItem);
    }

    /**
     * Opens README.md file
     */
    public void openReadme() {
        setDefaultRoot();
        open(readmeMdItem);
    }
}
