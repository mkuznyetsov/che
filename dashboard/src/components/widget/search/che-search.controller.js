/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

/**
 * This class is handling the controller for the search
 * @author Ann Shumilova
 */
export class CheSearchController {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($scope, $element) {
    this.$scope = $scope;
    this.$element = $element;
    this.isShown = false;
  }

  /**
   * Performs showing the search input and
   * hides replacement element (place where input will be shown).
   */
  show() {
    if (this.isShown) {
      return;
    }
    if (this.replaceElement) {
      let element = angular.element('#' + this.replaceElement);
      element.css('display', 'none');
    }
    this.$element.css('flex', '1 1');
    this.$element.find('input').focus();
    this.isShown = true;
  }

  /**
   * Hides the search input and displays back the replacement element.
   */
  hide() {
    this.isShown = false;
    if (this.replaceElement) {
      let element = angular.element('#' + this.replaceElement);
      element.css('display', 'flex');
    }
    this.$element.css('flex', 'none');
  }
}
