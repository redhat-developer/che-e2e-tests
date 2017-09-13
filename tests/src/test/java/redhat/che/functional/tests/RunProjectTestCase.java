///******************************************************************************* 
// * Copyright (c) 2017 Red Hat, Inc. 
// * Distributed under license by Red Hat, Inc. All rights reserved. 
// * This program is made available under the terms of the 
// * Eclipse Public License v1.0 which accompanies this distribution, 
// * and is available at http://www.eclipse.org/legal/epl-v10.html 
// * 
// * Contributors: 
// * Red Hat, Inc. - initial API and implementation 
// ******************************************************************************/
//package redhat.che.functional.tests;
//
//import static org.junit.Assert.*;
//
//import java.util.concurrent.TimeUnit;
//
//import org.jboss.arquillian.graphene.Graphene;
//import org.jboss.arquillian.graphene.findby.FindByJQuery;
//import org.jboss.arquillian.junit.Arquillian;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.openqa.selenium.WebElement;
//import org.openqa.selenium.support.FindBy;
//
//import redhat.che.functional.tests.fragments.CommandsManagerDialog;
//import redhat.che.functional.tests.fragments.Terminal;
//import redhat.che.functional.tests.fragments.ToolbarDebugPanel;
//import redhat.che.functional.tests.fragments.popup.ContextMenu;
//import redhat.che.functional.tests.fragments.popup.DropDownMenu;
//
//@RunWith(Arquillian.class)
//public class RunProjectTestCase extends AbstractCheFunctionalTest {
//
//    public static final String VERTX_JAR_FILE = "http-vertx-6-SNAPSHOT.jar";
//
//    @FindBy(id = "menu-lock-layer-id")
//    private ContextMenu contextMenu;
//    
//    @FindBy(id="gwt-debug-toolbarPanel")
//    private ToolbarDebugPanel debugPanel;
//    
//    @FindByJQuery("div[id=\"gwt-debug-commandConsoleLines\"]:last pre:contains('[INFO] BUILD SUCCESS')")
//    private WebElement successfulBuildPreElement;
//    
//    @FindBy(id = "menu-lock-layer-id")
//    private DropDownMenu dropDownMenu;
//    
//    @FindBy(id = "gwt-debug-consolesPanel")
//    private Terminal terminal;
//    
//    @FindByJQuery("div#commandsManagerView")
//    private CommandsManagerDialog commandsManagerDialog;
//    
//    @FindByJQuery("button#window-edit-commands-close")
//    private WebElement closeButton;
//    
//    @FindByJQuery("pre:contains('INFO: Succeeded in deploying verticle')")
//    private WebElement successfulVertxDeploy;
//    
//    private static final String KILL_VERTX_APP_COMMAND = 
//            "kill -9 $(ps -aux | grep 'vertx-6-SNAPSHOT.jar' | head -n1 | awk '{ print $2 }')";
//    
//    @Before
//    public void setup() {
//        openBrowser();
//        if (!isProjectBuilt()) {
//            buildProject();
//        }
//        
//        updateRunCommand();
//    }
//   
//    private void updateRunCommand() {
//        debugPanel.expandCommandsDropDown();
//        dropDownMenu.selectEditCommand();
//        commandsManagerDialog.updateCommandForJavaJar();
//        closeButton.click();
//        Graphene.waitModel().until().element(closeButton).is().not().present();
//    }
//   
//    @After
//    public void teardown() {
//        terminal.activateView();
//        terminal.executeCommands(KILL_VERTX_APP_COMMAND);
//    }
//    
////    @Test
//    public void runProjectTest() {
//        runProject();
//        terminal.activateView();
//        terminal.executeCommands("curl localhost:8080/api/greeting");
//
//        assertTrue("Terminal should contain hello world response from HTTP GET request,"
//                + " but it does not.", terminal.contains("Hello"));
//    }
//    
//    private void runProject() {
//        project.select();
//        debugPanel.expandCommandsDropDown();
//        dropDownMenu.selectRunCommand();
//        debugPanel.executeCommand();
//        Graphene.waitModel().until().element(successfulVertxDeploy).is().visible();
//    }
//    
//    /**
//     * Runs build command on the project.
//     */
//    private void buildProject() {
//        debugPanel.expandCommandsDropDown();
//        dropDownMenu.selectBuildCommand();
//        debugPanel.executeCommand();
//        waitUntilBuildIsSuccessfull();
//    }
//    
//    /**
//     * Waits 2 minutes until build is successfully finished.
//     */
//    private void waitUntilBuildIsSuccessfull() {
//        Graphene.waitModel().withTimeout(2, TimeUnit.MINUTES).until().element(successfulBuildPreElement).is().visible();
//    }
//    
//    /**
//     * Finds out whether a project is build or not. Checks by presence of JAR file
//     * in target directory.
//     * @return true if project is built, false otherwise
//     */
//    private boolean isProjectBuilt() {
//        if (project.resourceExists("target")) {
//            return project.getResource("target").resourceExists(VERTX_JAR_FILE);
//        }
//        return false;
//    }
//}
