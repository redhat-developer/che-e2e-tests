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
package redhat.che.functional.tests.utils;

import org.apache.log4j.Logger;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.redhat.arquillian.che.util.OpenShiftHelper;

/**
 * Logs Eclipse Che's console output (log), when test fails.
 * 
 * @author rhopp
 *
 */

public class GetCheLogsOnFailRule extends TestWatcher {

	@Override
	protected void failed(Throwable e, Description description) {
		Logger logger = Logger.getLogger(description.getClass());
		logger.error(OpenShiftHelper.getCheLogs());
	}
	
	

}
