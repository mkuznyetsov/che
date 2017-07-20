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
package test28;

class A {

    public String g;

    public String getF() {
        return getG();
    }

    public String getG() {
        return g;
    }

    public void setF(String f) {
        setG(f);
    }

    public void setG(String f) {
        this.g = f;
    }
}
