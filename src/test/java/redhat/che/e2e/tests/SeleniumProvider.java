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
package redhat.che.e2e.tests;

import java.io.File;
import java.io.IOException;

import org.openqa.selenium.chrome.ChromeDriverService;

public class SeleniumProvider {

	public static void setUpSeleniumChromeDriver() {
		String driverZipPath = Utils.downloadSeleniumChromeDriver();
		Utils.extractZipFile(driverZipPath);
		String pathToChromeDriver = Utils
				.setChromeDriverExecutable(driverZipPath.substring(0, driverZipPath.lastIndexOf(File.separator)));
		System.setProperty("webdriver.chrome.driver", pathToChromeDriver);
	}

	public static ChromeDriverService startChromeDriverService() {
		ChromeDriverService chromeService = ChromeDriverService.createDefaultService();
		try {
			chromeService.start();
		} catch (IOException e) {
			if (chromeService.isRunning()) {
				chromeService.stop();
			}
			throw new RuntimeException("Exception occured when starting chrome selenium service");
		}
		return chromeService;
	}

	public static void stopChromeDriverService(ChromeDriverService chromeService) {
		if (chromeService.isRunning()) {
			chromeService.stop();
		}
	}
	
}
