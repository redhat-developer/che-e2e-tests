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
package redhat.che.functional.tests.utils;

import static com.redhat.arquillian.che.config.CheExtensionConfiguration.OSIO_PASSWORD_PROPERTY_NAME;
import static com.redhat.arquillian.che.config.CheExtensionConfiguration.OSIO_USERNAME_PROPERTY_NAME;

public class Constants {

	// Current values
	public static final String OSIO_USERNAME = System.getProperty(OSIO_USERNAME_PROPERTY_NAME);
	public static final String OSIO_PASSWORD = System.getProperty(OSIO_PASSWORD_PROPERTY_NAME);
	public static final String PROJECT_NAME = "vertx-http-booster";
	public static final String TEST_FILE = "HttpApplicationTest.java";
	public static final String[] PATH_TO_TEST_FILE = new String[] { "src", "test", "java",
		"booster", TEST_FILE };
}
