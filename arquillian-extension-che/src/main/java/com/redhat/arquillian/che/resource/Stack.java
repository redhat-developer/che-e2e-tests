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

public enum Stack {

	VERTX("vert.x"),
	NODEJS("nodejs-centos");

	private String stack;

	private Stack(String status) {
		this.stack = status;
	}

	@Override
	public String toString() {
		return stack;
	}
}

