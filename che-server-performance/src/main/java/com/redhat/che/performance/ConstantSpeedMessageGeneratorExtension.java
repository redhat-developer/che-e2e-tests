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

import org.perfcake.message.generator.ConstantSpeedMessageGenerator;

/**
 * @author rhopp
 *
 */
public class ConstantSpeedMessageGeneratorExtension extends ConstantSpeedMessageGenerator {

	/* (non-Javadoc)
	 * @see org.perfcake.message.generator.DefaultMessageGenerator#getShutdownPeriod()
	 */
	@Override
	public long getShutdownPeriod() {
		return 60*30*1000;
	}
	
}
