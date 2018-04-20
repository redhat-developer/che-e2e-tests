/*******************************************************************************
 * Copyright (c) 2015-2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v 1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.arquillian.che.util;

import java.util.List;

import com.redhat.arquillian.che.config.CheExtensionConfiguration;

import io.fabric8.kubernetes.api.model.LabelSelectorBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;

/**
 * Helper class for directly interacting with OpenShift through API.
 * 
 * @author rhopp
 *
 */
public class OpenShiftHelper {

	/**
	 * Returns Eclipse Che pods logs.
	 * 
	 * @param openshiftMasterURL
	 * @param username
	 * @param password
	 * @param namespace
	 * @return
	 */
	public static String getCheLogs(String openshiftMasterURL, String username, String password, String namespace) {
		String output = "";
		Config config = new ConfigBuilder().withMasterUrl(openshiftMasterURL).withUsername(username)
				.withPassword(password).build();
		try (OpenShiftClient client = new DefaultOpenShiftClient(config)) {
			PodList list = client.pods().inNamespace(namespace)
					.withLabelSelector(new LabelSelectorBuilder().addToMatchLabels("deploymentconfig", "che").build())
					.list();
			List<Pod> items = list.getItems();
			for (Pod pod : items) {
				String name = pod.getMetadata().getName();
				String log = client.pods().inNamespace(namespace).withName(name).getLog();
				output += "Log for pod: " + name + "\n";
				output += log + "\n";
			}
		} catch (Exception e) {
			output += "Unable to retrieve error due to exception: " + e.getMessage();
		}
		return output;
	}

	/**
	 * Returns Eclipse Che pods logs. For openshift url, username, pass & namespace
	 * it uses system properties (defined here:
	 * {@link com.redhat.arquillian.che.config.CheExtensionConfiguration})
	 * 
	 * @return
	 */

	public static String getCheLogs() {
		String openshiftMasterURL = System.getProperty(CheExtensionConfiguration.CUSTOM_CHE_SERVER_FULL_URL_NAME).isEmpty()
				? System.getProperty(CheExtensionConfiguration.OPENSHIFT_MASTER_URL_PROPERTY_NAME)
				: System.getProperty(CheExtensionConfiguration.CUSTOM_CHE_SERVER_FULL_URL_NAME);
		String openshiftNamespace = System.getProperty(CheExtensionConfiguration.OPENSHIFT_NAMESPACE_PROPERTY_NAME);
		String osioUsername = System.getProperty(CheExtensionConfiguration.OSIO_USERNAME_PROPERTY_NAME);
		String osioPassword = System.getProperty(CheExtensionConfiguration.OSIO_PASSWORD_PROPERTY_NAME);
		return getCheLogs(openshiftMasterURL,osioUsername,osioPassword,openshiftNamespace);
	}
}
