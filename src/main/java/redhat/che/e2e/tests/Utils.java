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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class Utils {

	private static String driverWinURL = "https://chromedriver.storage.googleapis.com/2.27/chromedriver_win32.zip";
	private static String driverLinuxURL = "https://chromedriver.storage.googleapis.com/2.27/chromedriver_linux64.zip";
	private static String driverMacURL = "https://chromedriver.storage.googleapis.com/2.27/chromedriver_mac64.zip";

	/**
	 * Gets text from a file.
	 * @param fileToPath path to a file to get text from
	 * @return text in a file
	 */
	public static String getTextFromFile(String fileToPath) {
		StringBuilder sb = new StringBuilder();
		List<String> json;
		try {
			json = Files.readAllLines(Paths.get(fileToPath), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException("Cannot read file with JSON for a new workspace");
		}
		for (String str : json) {
			sb.append(str);
		}
		return sb.toString();
	}
	
	/**
	 * Extract file with a specified path to the same folder.
	 * 
	 * @param path path to a ZIP file to extract
	 */
	public static void extractZipFile(String path) {
		String destinationFolder = path.substring(0, path.lastIndexOf(File.separator));
		try {
			ZipFile zipFile = new ZipFile(path);
			zipFile.extractAll(destinationFolder);
		} catch (ZipException e) {
			e.printStackTrace();
			throw new RuntimeException("An error has occured while extracting zip file. "
					+ "Zip file cannot be extracted.");
		}
	}

	/** 
	 * Sets a file with specified path executable.
	 * @param path path to a directory with chrome driver
	 * @return path to driver
	 */
	public static String setChromeDriverExecutable(String chromeDir) {
		String pathToDriver;
		if (System.getProperty("os.name").toLowerCase().contains("win")) {
			pathToDriver = chromeDir + File.separator + "chromedriver.exe";
		} else {
			pathToDriver = chromeDir + File.separator + "chromedriver";
		}
		new File(pathToDriver).setExecutable(true);
		return pathToDriver;
	}
	
	/**
	 * Downloads chrome driver for a specific platform (Windows and Linux
	 * supported).
	 * 
	 * @return relative path to the driver on file system
	 */
	public static String downloadSeleniumChromeDriver() {
		String driverURL;
		String pathToDriverZIP = "." + File.separator + "resources" + File.separator + "chromedriver.zip";
		String osName = System.getProperty("os.name");
		if (osName.toLowerCase().contains("win")) {
			driverURL = driverWinURL;
		} else if (osName.toLowerCase().contains("nux")) {
			driverURL = driverLinuxURL;
		} else if (osName.toLowerCase().contains("mac")) {
			driverURL = driverMacURL;
		} else {
			throw new RuntimeException("Unknown OS, cannot proceed with downloading a driver");
		}
		URL driverWebsite;
		FileOutputStream fileOutputStream = null;

		new File("resources").mkdirs();
		try {
			driverWebsite = new URL(driverURL);
			ReadableByteChannel readableByteChannel = Channels.newChannel(driverWebsite.openStream());
			fileOutputStream = new FileOutputStream(pathToDriverZIP);
			fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
		} catch (IOException e) {
			throw new RuntimeException("Cannot download chrome driver.");
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
				}
			}
		}

		return pathToDriverZIP;
	}
	
	public static boolean isURLReachable(String URL) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection connection = (HttpURLConnection) new URL(URL).openConnection();
            connection.setRequestMethod("HEAD");
            return (connection.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            return false;
        }
    }
}
