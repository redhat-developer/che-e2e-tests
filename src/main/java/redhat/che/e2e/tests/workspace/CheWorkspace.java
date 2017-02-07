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
package redhat.che.e2e.tests.workspace;

public class CheWorkspace {
	
	private String id;
	private String name;
	private String URL;
	
	public CheWorkspace(String id, String name, String URL) {
		this.id = id;
		this.name = name;
		this.URL = URL;
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public String getURL() {
		return URL;
	}
}
