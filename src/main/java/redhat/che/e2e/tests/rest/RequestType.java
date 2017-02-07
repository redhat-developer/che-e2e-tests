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
package redhat.che.e2e.tests.rest;

public enum RequestType {

	GET("GET"),
	POST("POST"),
	PUT("PUT"),
	DELETE("DELETE"),
	OPTIONS("OPTIONS"),
	TRACE("TRACE"),
	CONNECT("CONNECT");
	
	private String request;
	
	private RequestType(String request) {
		this.request = request;
	}
	
	public static RequestType getRequest(String request) {
		switch (request) {
			case "GET": return GET;
			case "POST": return POST;
			case "PUT": return PUT;
			case "DELETE": return DELETE;
			case "OPTIONS": return OPTIONS;
			case "TRACE": return TRACE;
			case "CONNECT": return CONNECT;
			default: throw new IllegalArgumentException("Unknown request type");
		}	
	}
	
	public String getRequest() {
		return request;
	}
}
