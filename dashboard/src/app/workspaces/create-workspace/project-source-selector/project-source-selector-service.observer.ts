/*
 * Copyright (c) 2015-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

import {ProjectSource} from './project-source.enum';
import {ActionType} from './project-source-selector-action-type.enum';

export interface IProjectSourceSelectorServiceObserver {

  onProjectSourceSelectorServicePublish(action: ActionType, source: ProjectSource): void;

}
