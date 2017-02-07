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

public enum ObjectState {

	NEW("NEW"),
	EXISTING("EXISTING"),
	CUSTOM("CUSTOM");
	
	private String state;
	
	private ObjectState(String state) {
		this.state = state;
	};
	
	public String getState() {
		return state;
	}
	
	public static ObjectState getState(String state) {
		if (state.equals("NEW")) {
			return NEW;
		} else if (state.equals("EXISTING")) {
			return EXISTING;
		} else if (state.equals("CUSTOM")) {
			return CUSTOM;
		}
		return null;
	}
}
