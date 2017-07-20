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
package org.eclipse.che.selenium.projectexplorer;

import org.eclipse.che.selenium.core.project.ProjectTemplates;
import com.google.inject.Inject;

import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.ConfigureClasspath;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URL;
import java.nio.file.Paths;

/**
 * @author Andrey Chizhikov
 */
public class CreateProjectInSelectedFolderTest {

    private static final String PROJECT_NAME                  = CreateProjectInSelectedFolderTest.class.getSimpleName();
    private static final String PROJECT_NAME_WITH_ARTIFACT_ID = PROJECT_NAME + " [qa-spring-sample]";
    private static final String INNER_PROJECT_NAME            = "blank-project";
    private static final String PATH_TO_FOLDER                = PROJECT_NAME + "/blank-project";
    private static final String FOLDER_NAME                   = "blank-project";
    private static final String EXPECTED_TEXT                 = "You have created a blank project.";

    @Inject
    private TestWorkspace            testWorkspace;
    @Inject
    private DefaultTestUser          defaultTestUser;
    @Inject
    private Ide                      ide;
    @Inject
    private ProjectExplorer          explorer;
    @Inject
    private Loader                   loader;
    @Inject
    private CodenvyEditor            editor;
    @Inject
    private AskForValueDialog        askForValueDialog;
    @Inject
    private Wizard                   projectWizard;
    @Inject
    private ConfigureClasspath       selectPath;
    @Inject
    private Menu                     menu;
    @Inject
    private ProjectExplorer          projectExplorer;
    @Inject
    private TestProjectServiceClient testProjectServiceClient;

    @BeforeClass
    public void setUp() throws Exception {
        URL resource = getClass().getResource("/projects/default-spring-project");
        testProjectServiceClient.importProject(testWorkspace.getId(), defaultTestUser.getAuthToken(), Paths.get(resource.toURI()),
                                               PROJECT_NAME,
                                               ProjectTemplates.MAVEN_SPRING
        );
        ide.open(testWorkspace);
    }

    @Test
    public void createProjectInSelectedFolder() {
        explorer.waitProjectExplorer();
        explorer.openItemByPath(PROJECT_NAME);
        explorer.openContextMenuByPathSelectedItem(PROJECT_NAME);
        explorer.clickOnItemInContextMenu(TestProjectExplorerContextMenuConstants.NEW);
        explorer.clickOnNewContextMenuItem(TestProjectExplorerContextMenuConstants.SubMenuNew.FOLDER);
        askForValueDialog.waitFormToOpen();
        askForValueDialog.typeAndWaitText(FOLDER_NAME);
        askForValueDialog.clickOkBtn();
        loader.waitOnClosed();
        explorer.waitItem(PATH_TO_FOLDER);

        menu.runCommand(TestMenuCommandsConstants.Workspace.WORKSPACE, TestMenuCommandsConstants.Workspace.CREATE_PROJECT);

        projectWizard.selectSample(Wizard.TypeProject.BLANK);
        projectWizard.typeProjectNameOnWizard(INNER_PROJECT_NAME);

        projectWizard.clickOnSelectPathForParentBtn();
        selectPath.openItemInSelectPathForm("Workspace");
        selectPath.waitItemInSelectPathForm(PROJECT_NAME_WITH_ARTIFACT_ID);
        selectPath.selectItemInSelectPathForm(PROJECT_NAME_WITH_ARTIFACT_ID);
        selectPath.openItemInSelectPathForm(PROJECT_NAME_WITH_ARTIFACT_ID);
        selectPath.waitItemInSelectPathForm(FOLDER_NAME);
        selectPath.selectItemInSelectPathForm(FOLDER_NAME);
        selectPath.clickSelectBtnSelectPathForm();

        projectWizard.clickCreateButton();
        loader.waitOnClosed();

        projectExplorer.quickExpandWithJavaScript();
        explorer.openItemByPath(PROJECT_NAME + "/" + FOLDER_NAME + "/" + INNER_PROJECT_NAME + "/README");
        editor.waitActiveEditor();
        editor.waitTextIntoEditor(EXPECTED_TEXT);
    }

}
