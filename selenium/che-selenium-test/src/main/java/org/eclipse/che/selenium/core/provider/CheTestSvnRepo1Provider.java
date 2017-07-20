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
package org.eclipse.che.selenium.core.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.inject.Named;

import static java.lang.String.format;

/**
 * @author Dmytro Nochevnov
 */
@Singleton
public class CheTestSvnRepo1Provider implements TestSvnRepo1Provider {
    @Inject
    @Named("sys.docker.interface.ip")
    private String localSvnServerHost;

    @Inject
    @Named("sys.svn.server.port")
    private int localSvnServerPort;

    @Inject(optional = true)
    @Named("svn.repo_1.url")
    private String svnRepo1Url;

    @Override
    public String get() {
        if (svnRepo1Url != null) {
            return svnRepo1Url;
        }

        return getLocalServerUrl() + "qa";
    }

    private String getLocalServerUrl() {
        return format("http://%s:%s/svn/", localSvnServerHost, localSvnServerPort);
    }
}
