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

public class CheWorkspace {

	private String workspaceIDEURL;
	private String workspaceURL;
	private String workspaceRuntimeURL;
	
	/**
	 * Creates a new Che workspace.
	 * 
	 * @param workspaceIDEURL workspace IDE URL
	 * @param workspaceURL self link
	 * @param workspaceRuntimeURL workspace runtime URL (to operate states)
	 */
	public CheWorkspace(String workspaceIDEURL, String workspaceURL, String workspaceRuntimeURL) {
		this.workspaceIDEURL = workspaceIDEURL;
		this.workspaceURL = workspaceURL;
		this.workspaceRuntimeURL = workspaceRuntimeURL;
	}

	public String getWorkspaceURL() {
	    return workspaceURL;
	}
	
	public String getWorkspaceIDEURL() {
		return workspaceIDEURL;
	}
	
	public String getWorkspaceRuntimeURL() {
		return workspaceRuntimeURL;
	}
	
	@Override
	public String toString() {
		return "workspace accessible at " + workspaceIDEURL;
	}
}
