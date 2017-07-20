/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.examples;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * JUnit 3x Test Suite
 */
public class Junit3TestSuite {

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Test Main");
        suite.addTestSuite(AppOneTest.class);
        suite.addTestSuite(AppAnotherTest.class);

        return suite;
    }

}
