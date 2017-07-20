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
package org.eclipse.che.selenium.core.user;

/**
 * Represents a user in a test environment.
 *
 * @author Anatolii Bazko
 */
public interface TestUser {
    /**
     * Returns user's email.
     */
    String getEmail();

    /**
     * Returns user's password.
     */
    String getPassword();

    /**
     * Returns the current authentication token of the user.
     * Will be changed after login/logout procedure.
     */
    String getAuthToken();

    /**
     * Return user's name.
     */
    String getName();

    /**
     * Return user's id.
     */
    String getId();

    /**
     * Deletes user and its stuff.
     */
    void delete();
}
