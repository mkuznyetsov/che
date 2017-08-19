/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.docker.machine.node;

import java.io.IOException;

/**
 * Finds path to workspace folder on host.
 *
 * @author Alexander Garagatyi
 */
public interface WorkspaceFolderPathProvider {
  String getPath(String workspaceId) throws IOException;
}
