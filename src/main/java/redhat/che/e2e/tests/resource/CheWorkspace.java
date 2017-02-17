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
	
	private String id;
	private String name;
	private String workspaceURL;
	private String serverURL;
	private String wsAgentURL;
	
	/**
	 * Creates a new Che workspace.
	 * @param id id of a workspace
	 * @param name name of a workspace
	 * @param workspaceURL workspace IDE URL
	 * @param serverURL URL of Che server where this workspace belongs to
	 */
	public CheWorkspace(String id, String name, String workspaceURL, String serverURL) {
		this.id = id;
		this.name = name;
		this.workspaceURL = workspaceURL;
		this.serverURL = serverURL;
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public String getWorkspaceURL() {
		return workspaceURL;
	}
	
	public String getServerURL() {
		return serverURL;
	}
	
	public String getWsAgentURL() {
		return wsAgentURL;
	}
	
	public void setWsAgentURL(String wsAgentURL) {
		this.wsAgentURL = wsAgentURL;
	}
	
	@Override
	public String toString() {
		return "workspace " + name + " with ID " + id + " accessible at " + workspaceURL;
	}
}
