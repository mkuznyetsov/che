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
package test11;
import Test.Element;

import java.util.List;

class Test {
    static class Element{
    }

    static class A {
        private final List<Element> fElements;

        public A(List<Element> list) {
            fElements= list;
        }
        public List<Element> getList() {
            return fElements;
        }
        public void setList(List<Element> newList) {
            fElements= newList;
        }
    }

    {
        A a= new A(new List<Element>());
        a.setList(a.getList());
    }
}
