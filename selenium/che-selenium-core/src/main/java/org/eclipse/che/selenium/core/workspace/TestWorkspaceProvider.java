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
package org.eclipse.che.selenium.core.workspace;

import org.eclipse.che.selenium.core.user.TestUser;

/**
 * Workspace provider.
 *
 * @author Anatolii Bazko
 */
public interface TestWorkspaceProvider {

    /**
     * Creates a new workspace.
     *
     * @param owner
     *      the workspace owner
     * @param memoryGB
     *      the workspace memory size in GB
     * @param template
     *      the workspace template
     *      {@link WorkspaceTemplate}
     */
    TestWorkspace createWorkspace(TestUser owner, int memoryGB, String template);

    /**
     * Release all allocated resources.
     */
    void shutdown();
}
