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
package redhat.che.functional.tests.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import redhat.che.functional.tests.AnalyticsErrorMarkersTestCase;
import redhat.che.functional.tests.GitTestCase;
import redhat.che.functional.tests.MavenTestCase;
import redhat.che.functional.tests.PackageJsonTestCase;
import redhat.che.functional.tests.PomTestCase;
import redhat.che.functional.tests.WorkspacesTestCase;

/**
 * @author kkanova
 *
 */

@RunWith(Suite.class)
@SuiteClasses({
    //vert.x projects
    GitTestCase.class,
    PomTestCase.class,
    AnalyticsErrorMarkersTestCase.class,
    MavenTestCase.class,
    WorkspacesTestCase.class,

    //nodejs projects
    PackageJsonTestCase.class
})

public class OrderedTestSuite {

}
