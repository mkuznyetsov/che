/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.projectimport;

import org.eclipse.che.ide.commons.exception.JobNotFoundException;
import org.eclipse.che.ide.commons.exception.UnauthorizedException;

/**
 * The class contains business logic which allows define type of error.
 *
 * @author Dmitry Shnurenko
 */
public class ErrorMessageUtils {

    private ErrorMessageUtils() {
        throw new UnsupportedOperationException("You can not create instance of Util class.");
    }

    /**
     * The method defines error type and returns error message from passed exception.
     *
     * @param exception
     *         passed exception
     * @return error message
     */
    public static String getErrorMessage(Throwable exception) {
        if (exception instanceof JobNotFoundException) {
            return "Project import failed";
        } else if (exception instanceof UnauthorizedException) {
            UnauthorizedException unauthorizedException = (UnauthorizedException)exception;
            return getMessageFromJSON(unauthorizedException.getResponse().getText());
        } else {
            return getMessageFromJSON(exception.getMessage());
        }
    }

    private static native String getMessageFromJSON(String json) /*-{
        try {
            return JSON.parse(json).message;
        } catch (e) {
            return "";
        }
    }-*/;
}
