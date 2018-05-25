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
package com.redhat.arquillian.che.resource;

import com.redhat.arquillian.che.util.Constants;

public class StackService {

	public static String getPathOfJsonConfig(Stack stack){
		if (stack == Stack.VERTX)
			return Constants.CREATE_WORKSPACE_REQUEST_VERTX_JSON;
		if (stack == Stack.NODEJS)
			return Constants.CREATE_WORKSPACE_REQUEST_NODEJS_JSON;
		return null;

	}

	public static Stack getStackType(String projectType){
		if(projectType.equals("maven")) return Stack.VERTX;
		if(projectType.equals("node-js")) return Stack.NODEJS;
		if(projectType.equals("none")) return Stack.NONE;
		return null;
	}
}
