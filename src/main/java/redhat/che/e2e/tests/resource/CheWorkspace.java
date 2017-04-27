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

	private String ideLink;
	private String selfLink;
	private String runtimeLink;
	
	/**
	 * Creates a new Che workspace containing links to various endpoints.
	 * 
	 * @param ideLink
	 * @param selfLink
	 * @param runtimeLink
	 */
	public CheWorkspace(String ideLink, String selfLink, String runtimeLink) {
	   this.ideLink = ideLink;
	   this.selfLink = selfLink;
	   this.runtimeLink = runtimeLink;
	}
	
	public String getIdeLink() {
        return ideLink;
    }

    public void setIdeLink(String ideLink) {
        this.ideLink = ideLink;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }

    public String getRuntimeLink() {
        return runtimeLink;
    }

    public void setRuntimeLink(String runtimeLink) {
        this.runtimeLink = runtimeLink;
    }

    @Override
	public String toString() {
		return "workspace accessible at " + ideLink;
	}
}
