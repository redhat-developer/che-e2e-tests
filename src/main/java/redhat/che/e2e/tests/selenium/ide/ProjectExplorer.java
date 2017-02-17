/******************************************************************************* 
 * Copyright (c) 2017 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
*/
package redhat.che.e2e.tests.selenium.ide;

import org.openqa.selenium.By;
import org.openqa.selenium.By.ById;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Project explorer located in Che Web IDE.
 * 
 * @author mlabuda
 */
public class ProjectExplorer {

	private static final String REFRESH_BUTTON_ID = "gwt-debug-refreshSelectedPath";
	private static final String PROJECT_EXPLORER_TREE_ID = "gwt-debug-projectTree";
	private static final String CONTEXT_MENU_ID = "gwt-debug-contextMenu/newGroup";

	private WebDriver driver;

	public ProjectExplorer(WebDriver driver) {
		this.driver = driver;
	}

	/**
	 * Waits until an item with specic text is visible in project explorer
	 * 
	 * @param text text of an item
	 */
	private void waitUntilItemIsVisible(String text) {
		String locator = String.format("//div[@id='gwt-debug-projectTree']//div[text()='%s']", text);
		new WebDriverWait(driver, Timeouts.REDRAW)
				.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
	}

	/**
	 * Opens an item in project explorer. Parent items have to be expanded and
	 * visible.
	 * 
	 * @param path path to an item
	 */
	private void openItem(String path) {
		String locator = "//div[@path='%s']/div";
		WebElement item = new WebDriverWait(driver, Timeouts.REDRAW)
				.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(String.format(locator, path))));
		try {
			item.click();
			new Actions(driver).doubleClick(item).perform();
		} catch (StaleElementReferenceException ex) {
			refresh();
			item.click();
			new Actions(driver).doubleClick(item).perform();
		}
		revalidate();
	}

	/**
	 * Opens an item in project explorer with specific path.
	 * 
	 * @param path
	 *            path to an item
	 * @return path to the item
	 */
	private void openItem(String... path) {
		String tmpPath = "";
		for (String text : path) {
			tmpPath += "/" + text;
			openItem(tmpPath);
		}
	}

	/**
	 * Selects item with specified path which consists of visible texts of items in project explorer.
	 * 
	 * @param pathToItem path to an item
	 */
	public void selectItem(String... pathToItem) {
		openItem(pathToItem);
		selectItem(constructPath(pathToItem));
	}

	/**
	 * Selects an item in project explorer. Parent items have to be expanded and
	 * visible.
	 * 
	 * @param path
	 *            path to an item
	 */
	private void selectItem(String path) {
		String locator = "//div[@path='" + path + "']/div";
		String[] items = path.split("/");
		waitUntilItemIsVisible(items[items.length - 1]);
		WebElement item = driver.findElement(By.xpath(locator));
		item.click();
	}

	/**
	 * Opens context menu on an item with specified path. Item has to be visible 
	 * in project explorer and selected.
	 * 
	 * @param pathToItem path to an item
	 */
	public void openContextMenuOnItem(String... pathToItem) {
		String locator = "//div[@path='" + constructPath(pathToItem) + "']/div";
		new WebDriverWait(driver, Timeouts.REDRAW)
				.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
		WebElement node = driver.findElement(By.xpath(locator));
		node.click();
		Actions act = new Actions(driver);
		Action rClick = act.contextClick(node).build();
		rClick.perform();
		rClick.perform();
		waitOnContextMenu();
	}

	private void waitOnContextMenu() {
		new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.id(CONTEXT_MENU_ID)));
	}

	private void waitForContextMenuItemAndClickOnIt(String item) {
		new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.id(item))).click();
	}

	/**
	 * Selects context menu item with specified path. Context menu has to be opened in order
	 * to select a specific context menu item.
	 * 
	 * @param pathToContextMenuItem path to context menu item in context menu
	 */
	public void selectContextMenuItem(String... pathToContextMenuItem) {
		for (String item : pathToContextMenuItem) {
			waitForContextMenuItemAndClickOnIt(item);
		}
		waitUntilContextMenuIsClosed();
	}

	/**
	 * Constructs a path from visible texts of items in project explorer.
	 * @param path path consisting of texts of items
	 * @return path usable in searching via xpath using path attribute
	 */
	private String constructPath(String... path) {
		StringBuilder sb = new StringBuilder();
		for (String text: path) {
			sb.append("/");
			sb.append(text);
		}
		return sb.toString();
	}

	private void waitUntilContextMenuIsClosed() {
		new WebDriverWait(driver, 10).until(ExpectedConditions.invisibilityOfElementLocated(By.id(CONTEXT_MENU_ID)));
	}

	/**
	 * Refreshes project explorer.
	 */
	public void refresh() {
		new WebDriverWait(driver, 10)
				.until(ExpectedConditions.visibilityOf(driver.findElement(new ById(REFRESH_BUTTON_ID)))).click();
	}

	/**
	 * Revalidates project explorer tree.
	 */
	private void revalidate() {
		new WebDriverWait(driver, Timeouts.REVALIDATING)
				.until(ExpectedConditions.visibilityOf(driver.findElement(new ById(PROJECT_EXPLORER_TREE_ID))));
	}
}
