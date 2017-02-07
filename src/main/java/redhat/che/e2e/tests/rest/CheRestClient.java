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

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Request.Builder;

public class CheRestClient {

	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	
	private OkHttpClient client = new OkHttpClient();
	private String cheServerURL;
	
	
	public CheRestClient(String cheServerURL) {
		this.client = new OkHttpClient();
		this.cheServerURL = cheServerURL;
	}
	
	public Response sentRequest(String path, RequestType requestType) {
		return sentRequest(path, requestType, null);
	}
	
	public Response sentRequest(String path, RequestType requestType, String jsonRequestBody) {
		RequestBody body = jsonRequestBody != null ? RequestBody.create(JSON, jsonRequestBody) :
			RequestBody.create(null, new byte[0]);;
		Builder requestBuilder = new Request.Builder().url(cheServerURL + path);
		Request request = null;
		switch (requestType.getRequest()) {
			case ("GET"): request = requestBuilder.get().build(); break;
			case ("POST"): request = requestBuilder.post(body).build(); break;
			case ("PUT"): request = requestBuilder.put(body).build(); break;
			case ("DELETE"): request = (body == null) ? 
					requestBuilder.delete().build() : requestBuilder.delete(body).build(); break;
			default: request = null;
		}
		if (request == null) {
			throw new RuntimeException("Request is null. It was not possible to process specified request");
		}
		try {
			return client.newCall(request).execute();
		} catch (IOException e) {
			return null;
		} 
	}
}
