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
package org.eclipse.che.selenium.plainjava;

import org.eclipse.che.selenium.core.project.ProjectTemplates;
import com.google.inject.Inject;

import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsEditor;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsExplorer;
import org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URL;
import java.nio.file.Paths;

import static org.eclipse.che.selenium.pageobject.AskForValueDialog.JavaFiles.CLASS;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkersType.ERROR_MARKER;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.NEW;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.SubMenuNew.JAVA_CLASS;

/**
 * @author Musienko Maxim
 * @author Aleksandr Shmaraiev
 */
public class ConfigureSomeSourceFoldersTest {
    private static final String PROJECT_NAME     = NameGenerator.generate("PlainJava-", 4);
    private              String newJavaClassName = "NewClass";

    @Inject
    private TestWorkspace        ws;
    @Inject
    private Ide                      ide;
    @Inject
    private DefaultTestUser          productUser;
    @Inject
    private ProjectExplorer          projectExplorer;
    @Inject
    private CodenvyEditor            codenvyEditor;
    @Inject
    private Loader                   loader;
    @Inject
    private AskForValueDialog        askForValueDialog;
    @Inject
    private CommandsExplorer         commandsExplorer;
    @Inject
    private CommandsEditor           commandsEditor;
    @Inject
    private Consoles                 consoles;
    @Inject
    private TestProjectServiceClient testProjectServiceClient;

    @BeforeClass
    public void prepare() throws Exception {
        URL resource = getClass().getResource("/projects/java-project-with-additional-source-folder");
        testProjectServiceClient.importProject(ws.getId(), productUser.getAuthToken(), Paths.get(resource.toURI()), PROJECT_NAME,
                                               ProjectTemplates.PLAIN_JAVA
        );
        ide.open(ws);
    }

    @Test
    public void checkConfigureClasspathPlainJavaProject() {
        projectExplorer.waitProjectExplorer();
        projectExplorer.waitItem(PROJECT_NAME);
        projectExplorer.openItemByPath(PROJECT_NAME);
        projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + "/source");
        projectExplorer.clickOnItemInContextMenu(TestProjectExplorerContextMenuConstants.BUILD_PATH);
        projectExplorer.clickOnItemInContextMenu(TestProjectExplorerContextMenuConstants.SubMenuBuildPath.USE_AS_SOURCE_FOLDER);
        projectExplorer.waitFolderDefinedTypeOfFolderByPath(PROJECT_NAME + "/source", ProjectExplorer.FolderTypes.JAVA_SOURCE_FOLDER);
        projectExplorer.waitFolderDefinedTypeOfFolderByPath(PROJECT_NAME + "/src", ProjectExplorer.FolderTypes.JAVA_SOURCE_FOLDER);
        projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + "/source");
        createNewJavaClass(newJavaClassName);
        projectExplorer.waitItem(PROJECT_NAME + "/source/" + newJavaClassName + ".java");
        codenvyEditor.waitTextIntoEditor("public class NewClass {");
        codenvyEditor.waitAllMarkersDisappear(ERROR_MARKER);
        codenvyEditor.setCursorToDefinedLineAndChar(2, 24);
        codenvyEditor.typeTextIntoEditor(Keys.ENTER.toString());
        String methodForChecking = " public static String typeCheckMess(){\n" +
                                   "        return \"Message from source folder\";\n" +
                                   "    ";
        codenvyEditor.typeTextIntoEditor(methodForChecking);
        codenvyEditor.waitAllMarkersDisappear(ERROR_MARKER);
        projectExplorer.openItemByPath(PROJECT_NAME + "/src");
        projectExplorer.waitItem(PROJECT_NAME + "/src/Main.java");
        projectExplorer.openItemByPath(PROJECT_NAME + "/src/Main.java");
        codenvyEditor.waitTabFileWithSavedStatus("Main");
        launchMainClassFromCommandWidget();
        consoles.waitExpectedTextIntoConsole("Message from source", 15);
    }

    private void createNewJavaClass(String name) {
        projectExplorer.clickOnItemInContextMenu(NEW);
        projectExplorer.clickOnNewContextMenuItem(JAVA_CLASS);
        askForValueDialog.createJavaFileByNameAndType(name, CLASS);
        projectExplorer.waitItemInVisibleArea(name + ".java");
        codenvyEditor.waitActiveEditor();
        loader.waitOnClosed();
        codenvyEditor.waitTabIsPresent(name);
    }

    private void launchMainClassFromCommandWidget() {
        commandsExplorer.openCommandsExplorer();
        commandsExplorer.waitCommandExplorerIsOpened();
        commandsExplorer.clickAddCommandButton(TestIntelligentCommandsConstants.CommandsGoals.RUN_GOAL);
        commandsExplorer.chooseCommandTypeInContextMenu(TestIntelligentCommandsConstants.CommandsTypes.JAVA_TYPE);
        commandsExplorer.checkCommandIsPresentInGoal(TestIntelligentCommandsConstants.CommandsGoals.RUN_GOAL,
                                                     TestIntelligentCommandsConstants.CommandsDefaultNames.JAVA_NAME);
        commandsEditor.waitTabFileWithSavedStatus(TestIntelligentCommandsConstants.CommandsDefaultNames.JAVA_NAME);
        commandsEditor.clickOnRunButton();
    }

}
