/******************************************************************************* 
 * Copyright (c) 2018 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
*/
package com.redhat.che.performance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.perfcake.PerfCakeException;
import org.perfcake.message.Message;
import org.perfcake.message.sender.AbstractSender;
import org.perfcake.message.sender.HttpSender;
import org.perfcake.message.sender.HttpSender.Method;
import org.perfcake.reporting.MeasurementUnit;
import org.perfcake.util.Utils;

import com.jayway.jsonpath.JsonPath;
import com.squareup.okhttp.internal.http.HttpConnection;

import net.minidev.json.JSONArray;

/**
 * @author rhopp
 *
 */
public class OperateCheWorkspaceSender extends AbstractSender {

	HttpURLConnection createWorkspaceConnection;
	HttpURLConnection deleteWorkspaceConnection;

	private String cheStarterUrl;
	
	private String token;
	
	private String propetiesFile;
	
	private static int threadCounter=0;
	private int currentThread;

	
	public String getToken() {
		return token;
	}
	
	public OperateCheWorkspaceSender setPropertiesFile(final String propertiesFile) {
		this.propetiesFile = propertiesFile;
		return this;
	}
	
	public String getPropertiesFile() {
		return propetiesFile;
	}
	
	public OperateCheWorkspaceSender setToken(String token) {
		this.token = token;
		return this;
	}

	private static final Logger log = LogManager.getLogger(OperateCheWorkspaceSender.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.perfcake.message.sender.AbstractSender#doClose()
	 */
	@Override
	public void doClose() throws PerfCakeException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.perfcake.message.sender.AbstractSender#doInit(java.util.Properties)
	 */
	@Override
	public void doInit(Properties messageAttributes) throws PerfCakeException {
		currentThread = threadCounter++;
		try {
			log.info(new File(".").getAbsolutePath());
			token = loadToken();
		} catch (IOException e) {
			throw new PerfCakeException("Unable to initialize properties file", e);
		}
		
		final String targetUrl = safeGetTarget(messageAttributes);
		if (log.isDebugEnabled()) {
			log.debug("Setting target URL to: " + targetUrl);
		}
		cheStarterUrl = targetUrl;
	}

	/**
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	private String loadToken() throws FileNotFoundException, IOException {
		log.info(propetiesFile);
		Properties properties = new Properties();
		properties.load(new FileInputStream(new File(propetiesFile).getAbsolutePath()));
		return (String) properties.get("user"+currentThread);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.perfcake.message.sender.AbstractSender#preSend(org.perfcake.message.
	 * Message, java.util.Properties)
	 */
	@Override
	public void preSend(Message message, Properties messageAttributes) throws Exception {
		// TODO Auto-generated method stub
		super.preSend(message, messageAttributes);
		log.info("Pre-send");
		createWorkspaceConnection = createConnection(cheStarterUrl + "/workspace?masterUrl=test&namespace=test",
				Method.POST);
		createWorkspaceConnection.setDoOutput(true);
	}

	private HttpURLConnection createGetWorkspaceConnection() throws IOException {
		return createConnection(cheStarterUrl + "/workspace?masterUrl=test&namespace=test", Method.GET);
	}

	/**
	 * @param string
	 * @return
	 * @throws IOException
	 */
	private HttpURLConnection createConnection(String url, Method method) throws IOException {
		URL url2 = new URL(url);
		HttpURLConnection connection = (HttpURLConnection) url2.openConnection();
		connection.setRequestMethod(method.name());
		connection.setDoInput(true);
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("Authorization",
				"Bearer "+getToken());
		return connection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.perfcake.message.sender.AbstractSender#doSend(org.perfcake.message.
	 * Message, org.perfcake.reporting.MeasurementUnit)
	 */
	@Override
	public Serializable doSend(Message message, MeasurementUnit mu) throws Exception {
		log.info("do Send");

		createWorkspaceConnection.connect();
		final OutputStreamWriter out = new OutputStreamWriter(createWorkspaceConnection.getOutputStream(),
				Utils.getDefaultEncoding());
		out.write(message.getPayload().toString(), 0, message.getPayload().toString().length());
		out.flush();
		out.close();
		createWorkspaceConnection.getOutputStream().close();

		String createdWorkspaceJson = getPayloadFromConnection(createWorkspaceConnection);
		log.info(createdWorkspaceJson);
		String workspaceId = "";
		try {
			workspaceId = JsonPath.read(createdWorkspaceJson, "$.id");
		} catch (Throwable t) {
			t.printStackTrace();
			log.info(t.getMessage());
			throw new PerfCakeException(t);
		}

		log.info("Workspace created with id: " + workspaceId);

		HttpURLConnection getWorkspacesConnection = createGetWorkspaceConnection();
		getWorkspacesConnection.connect();

		String workspaceStatus = getWorkspaceStatus(workspaceId, getWorkspacesConnection);
		boolean alreadyStarting = false;

		while (!workspaceStatus.equals("RUNNING")) {
			// Workspace has to be in state "STARTING" before we can check for state
			// "STOPPED".
			if (workspaceStatus.equals("STARTING")) {
				alreadyStarting = true;
			}
			// Workspace is STOPPED after it was "STARTING". This means workspace failed to
			// start.
			if (workspaceStatus.equals("STOPPED") && alreadyStarting) {
				throw new PerfCakeException("Workspace id: " + workspaceId + " failed to start");
			}
			getWorkspacesConnection = createGetWorkspaceConnection();
			getWorkspacesConnection.connect();
			workspaceStatus = getWorkspaceStatus(workspaceId, getWorkspacesConnection);
			Thread.sleep(1000);
		}
		return "";
	}

	private String getWorkspaceStatus(String workspaceId, HttpURLConnection connection) throws Exception {
		String workspacesJson = getPayloadFromConnection(connection);
		JSONArray statusArray = JsonPath.read(workspacesJson, "$[?(@.id == '" + workspaceId + "')].status");
		String workspaceStatus = (String) statusArray.get(0);
		return workspaceStatus;
	}

	private String getPayloadFromConnection(HttpURLConnection connection)
			throws IOException, PerfCakeException, UnsupportedEncodingException {
		InputStream rcis;
		int responseCode = createWorkspaceConnection.getResponseCode();
		if (responseCode < 400) {
			rcis = connection.getInputStream();
		} else {
			rcis = connection.getErrorStream();
			throw new PerfCakeException(getPayloadFromInputStream(rcis));
		}

		String payload = getPayloadFromInputStream(rcis);
		return payload;
	}

	private String getPayloadFromInputStream(InputStream rcis) throws UnsupportedEncodingException, IOException {
		String payload = null;
		if (rcis != null) {
			final char[] cbuf = new char[10 * 1024];
			final InputStreamReader read = new InputStreamReader(rcis, Utils.getDefaultEncoding());
			// note that Content-Length is available at this point
			final StringBuilder sb = new StringBuilder();
			int ch = read.read(cbuf);
			while (ch != -1) {
				sb.append(cbuf, 0, ch);
				ch = read.read(cbuf);
			}
			read.close();
			rcis.close();
			payload = sb.toString();
		}
		return payload;
	}

}
