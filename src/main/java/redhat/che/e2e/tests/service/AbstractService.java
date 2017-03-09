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
package redhat.che.e2e.tests.service;

import java.io.IOException;

import com.jayway.jsonpath.Configuration;

import okhttp3.Response;

public abstract class AbstractService {

	// Interval between querying
	protected static long SLEEP_TIME_TICK = 2000;
	// Wait time in seconds
	protected static int WAIT_TIME = 300;
	
	public static Object getDocumentFromResponse(Response response) {
		String responseString = null;
		if (response.isSuccessful()) {
			try {
				responseString = response.body().string();
			} catch (IOException e) {
			}
		}
		if (responseString == null) {
			throw new RuntimeException("Something went wrong and response is empty");
		}
		return Configuration.defaultConfiguration().jsonProvider().parse(responseString);
	}

}
