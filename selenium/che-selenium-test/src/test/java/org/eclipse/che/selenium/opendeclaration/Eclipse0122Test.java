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
package org.eclipse.che.selenium.opendeclaration;

import org.eclipse.che.selenium.core.project.ProjectTemplates;
import com.google.inject.Inject;

import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URL;
import java.nio.file.Paths;

/**
 * @author Maxim Musienko
 * @author Aleksandr Shmaraev
 */
public class Eclipse0122Test {

    private static final String PATH_TO_PACKAGE_PREFIX = "/src/main/java/org/eclipse/qa/examples/";
    private static final String PROJECT_NAME           = NameGenerator.generate(Eclipse0122Test.class.getSimpleName(), 4);

    @Inject
    private TestWorkspace            ws;
    @Inject
    private DefaultTestUser          defaultTestUser;
    @Inject
    private Ide                      ide;
    @Inject
    private ProjectExplorer          projectExplorer;
    @Inject
    private CodenvyEditor            editor;
    @Inject
    private TestProjectServiceClient testProjectServiceClient;

    @BeforeClass
    public void prepare() throws Exception {
        URL resource = getClass().getResource("/projects/resolveTests_1_5_t0122");
        testProjectServiceClient.importProject(ws.getId(), defaultTestUser.getAuthToken(), Paths.get(resource.toURI()), PROJECT_NAME,
                                               ProjectTemplates.MAVEN_SPRING);
        ide.open(ws);
    }

    @Test
    public void test0122() throws Exception {
        projectExplorer.waitProjectExplorer();
        projectExplorer.waitItem(PROJECT_NAME);
        projectExplorer.quickExpandWithJavaScript();
        projectExplorer.openItemByPath(PROJECT_NAME + PATH_TO_PACKAGE_PREFIX + "Test.java");
        editor.waitActiveEditor();
        editor.setCursorToDefinedLineAndChar(15, 24);
        editor.typeTextIntoEditor(Keys.F4.toString());
        editor.waitSpecifiedValueForLineAndChar(14, 16);
    }
}
