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
package com.redhat.arquillian.che.rest;

import java.io.IOException;

import org.apache.log4j.Logger;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RestClient {

	private static final Logger logger = Logger.getLogger(RestClient.class);
	
	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	private OkHttpClient client = new OkHttpClient();
	private String serverURL;

	public RestClient(String serverURL) {
		this.client = new OkHttpClient();
		this.serverURL = serverURL;
	}

	public void close() {
		try {
		client.dispatcher().executorService().shutdown();
		client.connectionPool().evictAll();
		client.cache().close();
		} catch (Exception ex) {
			
		}
	}

	public Response sentRequest(String path, RequestType requestType) {
		return sentRequest(path, requestType, null, null);
	}

	public Response sentRequest(String path, RequestType requestType, String jsonRequestBody) {
		return sentRequest(path, requestType, jsonRequestBody, null, (QueryParam[]) null);
	}

	public Response sentRequest(String path, RequestType requestType, String jsonRequestBody, String authorization,
			QueryParam... queryParams) {
		RequestBody body = jsonRequestBody != null ? RequestBody.create(JSON, jsonRequestBody)
				: RequestBody.create(null, new byte[0]);
		StringBuilder sb = new StringBuilder(serverURL);
		if (path != null) {
			sb.append(path);
		}
		if (queryParams != null && queryParams.length > 0) {
			sb.append("?");
			for (int i = 0; i < queryParams.length; i++) {
				sb.append(queryParams[i].getName() + "=" + queryParams[i].getValue());
				if (i != queryParams.length - 1) {
					sb.append("&");
				}
			}
		}
		Builder requestBuilder = new Request.Builder().url(sb.toString());
		requestBuilder.addHeader("Content-Type", "application/json");
		if (authorization != null && authorization.length() > 0) {
			requestBuilder.addHeader("Authorization", authorization);
		}
		Request request = null;
		switch (requestType.getRequest()) {
		case ("GET"):
			request = requestBuilder.get().build();
			break;
		case ("POST"):
			request = requestBuilder.post(body).build();
			break;
		case ("PUT"):
			request = requestBuilder.put(body).build();
			break;
		case ("DELETE"):
			request = (body == null) ? requestBuilder.delete().build() : requestBuilder.delete(body).build();
			break;
		default:
			request = null;
		}
		if (request == null) {
			throw new RuntimeException("Request is null. It was not possible to process specified request");
		}
		try {
			logger.info("Sending request: "+request.toString());
			Response response = client.newCall(request).execute();
			logger.info("Recieved response: "+response.toString());
			return response;
		} catch (IOException e) {
			logger.error("Error sending request.");
			logger.error(e.getMessage());
			return null;
		}
	}
}
