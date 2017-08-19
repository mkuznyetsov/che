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

/**
 * Defines a directive to allow to enter only specified subset of symbols.
 * @author Oleksii Kurinnyi
 */
export abstract class CheInputType {
  private restrict: string = 'A';

  link($scope: ng.IScope, $element: ng.IAugmentedJQuery, attrs: {cheInputType: string, [prop: string]: string}): void {

    $element.on('keydown', (event: KeyboardEvent) => {
      // escape, enter, tab
      if (event.keyCode === 27 || event.keyCode === 13 || event.keyCode === 9) {
        return true;
      }
      // delete, backspace
      if (event.keyCode === 46 || event.keyCode === 8) {
        return true;
      }
      // arrows
      if (event.keyCode === 37 || event.keyCode === 38 || event.keyCode === 39 || event.keyCode === 40) {
        return true;
      }
      // home, end
      if (event.keyCode === 36 || event.keyCode === 35) {
        return true;
      }
      // valid symbols
      return this.symbolIsValid(event.key);
    });
  }

  abstract symbolIsValid(symbol: string): boolean;

}
