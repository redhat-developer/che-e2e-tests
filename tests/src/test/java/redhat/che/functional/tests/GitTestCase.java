/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package redhat.che.functional.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.jayway.jsonpath.JsonPath;
import com.redhat.arquillian.che.annotations.Workspace;
import com.redhat.arquillian.che.resource.Stack;

import redhat.che.functional.tests.fragments.BottomInfoPanel;
import redhat.che.functional.tests.fragments.popup.Popup;
import redhat.che.functional.tests.fragments.topmenu.GitPopupTopMenu;
import redhat.che.functional.tests.fragments.topmenu.ProfileTopMenu;
import redhat.che.functional.tests.fragments.window.BranchesWindow;
import redhat.che.functional.tests.fragments.window.CommitToRepoWindow;
import redhat.che.functional.tests.fragments.window.GitPushWindow;
import redhat.che.functional.tests.fragments.window.PreferencesWindow;

@RunWith(Arquillian.class)
@Workspace(stackID = Stack.VERTX)
public class GitTestCase extends AbstractCheFunctionalTest {
	private static final Logger LOG = Logger.getLogger(GitTestCase.class);

	@FindByJQuery("table[title='Preferences']")
	private PreferencesWindow preferencesWindow;

	@FindBy(id = "menu-lock-layer-id")
	private ProfileTopMenu profileTopMenu;

	@FindBy(id = "menu-lock-layer-id")
	private GitPopupTopMenu gitPopupTopMenu;

	@FindBy(id = "gwt-debug-infoPanel")
	private BottomInfoPanel bottomInfoPanel;

	@FindBy(id = "gwt-debug-git-commit-window")
	private CommitToRepoWindow commitToRepoWindow;

	@FindBy(id = "gwt-debug-git-remotes-push-window")
	private GitPushWindow gitPushWindow;

	@FindBy(id = "gwt-debug-popup-container")
	private Popup popup;

	@FindBy(id = "gwt-debug-loader-message")
	private WebElement updatingPopup;

	@FindBy(id = "gwt-debug-git-branches-window")
	private BranchesWindow branchesWindow;

	private static String branchName;
	private static String remoteURL = "https://github.com/osiotest/vertx-http-booster.git";

	@BeforeClass
	public static void setupClass() {
		branchName = "Branch" + String.valueOf(new Date().getTime());
		LOG.info("Branch name: " + branchName);
	}
	
	@After
	public void after() {
		editorPart.tabsPanel().closeAllTabs(driver);
	}

	@AfterClass
	public static void teardownClass() {
		LOG.info("Cleaning up remote repo");
		Object ghTokenJson = provider.callGetGithubTokenEndpoint();
		String ghUsername = JsonPath.read(ghTokenJson, "$.username");
		String ghToken = JsonPath.read(ghTokenJson, "$.access_token");

		try {
			Git git = Git.init().setBare(true).call();
			RemoteAddCommand addRemoteCommand = git.remoteAdd();
			addRemoteCommand.setName("upstream");
			addRemoteCommand.setUri(new URIish(remoteURL));
			addRemoteCommand.call();

			RefSpec refSpec = new RefSpec().setSource(null).setDestination("refs/heads/" + branchName);
			git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(ghUsername, ghToken))
					.setRefSpecs(refSpec).setRemote("upstream").call();
		} catch (GitAPIException | URISyntaxException e) {
			LOG.error("JGit failure during cleanup", e);
			throw new RuntimeException(e);
		}
	}

	@Test
	@InSequence(1)
	public void test_load_ssh_key_and_set_commiter_information() {
		// set commiter credentials
		LOG.info("Starting: " + this.getClass().getName());
		openBrowser();
		waitUntilProjectImported("Project vertx-http-booster imported", 60);
		vertxProject.getResource("README.md").open();
		// get the "Updating project..." popup out of the way
		try {
			Graphene.waitGui().withTimeout(30, TimeUnit.SECONDS).until().element(updatingPopup).is().visible();
			Graphene.waitGui().withTimeout(30, TimeUnit.SECONDS).until().element(updatingPopup).is().not().visible();
		} catch (NoSuchElementException ex) {
			// Updating projects popup didn't show up. Nothing happens
		}

		LOG.info("Test: test_load_ssh_key_and_set_commiter_information");
		mainMenuPanel.clickProfile();
		profileTopMenu.openPreferences();
		preferencesWindow.writeCommiterInformation("test-user", "test@mail.com");
		preferencesWindow.close();
	}

	@Test
	@InSequence(2)
	public void create_and_switch_branch() {
		mainMenuPanel.clickGit();
		gitPopupTopMenu.branches();
		branchesWindow.waitForBeingVisible();
		branchesWindow.createBranch(branchName);
		branchesWindow.checkoutBranch(branchName);
	}

	@Test
	@InSequence(3)
	public void test_change_file_and_add_into_index() {
		// openBrowser(driver);
		vertxProject.getResource("README.md").open();
		editorPart.tabsPanel().waitUntilActiveTabHasName("README.md");
		String stringToAdd = "changes added on: " + new Date().toInstant().toEpochMilli();
		LOG.info("Writing string to README.md: \"" + stringToAdd + "\"");
		editorPart.codeEditor().writeIntoElementContainingString(stringToAdd, "changes added on:");
		mainMenuPanel.clickGit();
		gitPopupTopMenu.addToIndex();
		bottomInfoPanel.tabsPanel().waitUntilFocusedTabHasName(BottomInfoPanel.TabNames.TAB_GIT_ADD_TO_INDEX);
		try {
			bottomInfoPanel.waitUntilConsolePartContains(BottomInfoPanel.FixedConsoleText.GIT_ADDED_TO_INDEX_TEXT);
		} catch (TimeoutException ex) {
			LOG.error("Failure to edit file", ex);
			fail("Failure to edit file");
		}
	}

	@Test
	@InSequence(4)
	public void test_create_commit() {
		// openBrowser(driver);

		mainMenuPanel.clickGit();
		gitPopupTopMenu.commitSelected();
		commitToRepoWindow.addCommitMessage("changed README as part of a test that was run on: " + new Date());
		commitToRepoWindow.commit();
		bottomInfoPanel.tabsPanel().waitUntilFocusedTabHasName(BottomInfoPanel.TabNames.TAB_GIT_COMMIT);
		bottomInfoPanel.waitUntilConsolePartContains(BottomInfoPanel.FixedConsoleText.GIT_COMMITED_WITH_REVISION);
	}

	/*
	 * This test case will only work, when it is run with osio user who has push
	 * access to github repo.
	 */

	@Test
	@InSequence(5)
	public void test_push_changes() {
		// openBrowser(driver);
		mainMenuPanel.clickGit();
		gitPopupTopMenu.push();
		gitPushWindow.push(branchName, branchName);
		popup.waitForPopup("Pushed to origin");
	}

	@Test
	@InSequence(6)
	public void check_branch_is_pushed() {
		Collection<Ref> call;
		try {
			call = Git.lsRemoteRepository().setRemote(remoteURL).call();
		} catch (GitAPIException e) {
			LOG.error("JGit failure", e);
			throw new RuntimeException(e);
		}
		boolean found = false;
		for (Ref ref : call) {
			if (ref.getName().equals("refs/heads/" + branchName)) {
				found = true;
			}
		}
		assertTrue("Branch was not found on remote.", found);
	}
}
