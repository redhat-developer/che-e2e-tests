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
package redhat.che.e2e.tests.resource;

public class CheServer {

	private String URL;
	
	/**
	 * Creates a new Che Server by providing its URL
	 * 
	 * @param URL
	 */
	public CheServer(String URL) {
		this.URL = URL;
	}
	
	public String getURL() {
		return URL;
	}
}
