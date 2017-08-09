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
package org.eclipse.che.selenium.intelligencecommand;

import com.google.inject.Inject;

import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsEditor;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsExplorer;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URL;
import java.nio.file.Paths;

import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsGoals.RUN_GOAL;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsTypes.JAVA_TYPE;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsDefaultNames.JAVA_NAME;
import static org.eclipse.che.selenium.pageobject.intelligent.CommandsEditor.CommandsEditorType.COMMAND_LINE_EDITOR;
import static org.eclipse.che.selenium.pageobject.intelligent.CommandsEditor.CommandsEditorType.PREVIEW_URL_EDITOR;
import static org.eclipse.che.selenium.pageobject.intelligent.CommandsEditor.CommandsMacrosLinkType.EDITOR_MACROS_LINK;
import static org.eclipse.che.selenium.pageobject.intelligent.CommandsEditor.CommandsMacrosLinkType.PREVIEW_MACROS_LINK;

/**
 * @author Aleksandr Shmaraiev
 */
public class MacrosCommandsEditorTest {
    private static final String PROJ_NAME    = NameGenerator.generate("MacrosCommandsEditorTest-", 4);
    private static final String PATH_TO_FILE = PROJ_NAME + "/src/Main.java";

    @Inject
    private TestWorkspace            ws;
    @Inject
    private DefaultTestUser          defaultTestUser;
    @Inject
    private Ide                      ide;
    @Inject
    private ProjectExplorer          projectExplorer;
    @Inject
    private CommandsEditor           commandsEditor;
    @Inject
    private CommandsExplorer         commandsExplorer;
    @Inject
    private Loader                   loader;
    @Inject
    private Consoles                 consoles;
    @Inject
    private TestProjectServiceClient testProjectServiceClient;

    @BeforeClass
    public void prepare() throws Exception {
        URL resource = getClass().getResource("/projects/java-project-with-additional-source-folder");
        testProjectServiceClient.importProject(ws.getId(),
                                               defaultTestUser.getAuthToken(),
                                               Paths.get(resource.toURI()),
                                               PROJ_NAME,
                                               ProjectTemplates.PLAIN_JAVA);
        ide.open(ws);
    }

    @Test(priority = 1)
    public void checkCommandMacrosIntoCommandLine() {
        projectExplorer.waitProjectExplorer();
        projectExplorer.waitItem(PROJ_NAME);
        projectExplorer.quickExpandWithJavaScript();
        projectExplorer.waitItem(PATH_TO_FILE);
        projectExplorer.openItemByPath(PATH_TO_FILE);
        createNewJavaCommand();
        commandsEditor.selectMacrosLinkInCommandsEditor(EDITOR_MACROS_LINK);
        commandsEditor.cancelFormInEditorByEscape();
        commandsEditor.waitCommandsMacrosIsClosed();
        commandsEditor.setFocusIntoTypeCommandsEditor(COMMAND_LINE_EDITOR);
        commandsEditor.setCursorToLine(1);
        commandsEditor.typeTextIntoEditor(Keys.ENTER.toString());
        commandsEditor.typeTextIntoEditor(Keys.ARROW_UP.toString());
        commandsEditor.waitActiveEditor();
        commandsEditor.typeTextIntoEditor("echo ");
        commandsEditor.selectMacrosLinkInCommandsEditor(EDITOR_MACROS_LINK);
        commandsEditor.typeTextIntoSearchMacroField("rel");
        commandsEditor.waitTextIntoSearchMacroField("rel");
        String[] macrosItems = {"${current.project.relpath}", "${editor.current.file.relpath}", "${explorer.current.file.relpath}",
                                "Path relative to the /projects folder to the selected file in editor",
                                "Path relative to the /projects folder in project tree"};
        for (String macrosItem : macrosItems) {
            commandsEditor.waitTextIntoMacrosContainer(macrosItem);
        }
        commandsEditor.enterMacroCommandByEnter("${explorer.current.file.relpath}");
        commandsEditor.waitTextIntoEditor("echo ${explorer.current.file.relpath}");
        commandsEditor.clickOnRunButton();
        consoles.waitExpectedTextIntoConsole("/" + PROJ_NAME + "/src/Main.java");
        commandsEditor.setCursorToLine(1);
        commandsEditor.selectLineAndDelete();
        commandsEditor.waitActiveEditor();
        commandsEditor.typeTextIntoEditor("echo ");
        commandsEditor.selectMacrosLinkInCommandsEditor(EDITOR_MACROS_LINK);
        commandsEditor.selectMacroCommand("${current.class.fqn}");
        commandsEditor.typeTextIntoEditor(Keys.ARROW_DOWN.toString());
        commandsEditor.typeTextIntoEditor(Keys.SPACE.toString());
        commandsEditor.waitMacroCommandIsSelected("${current.project.path}");
        commandsEditor.enterMacroCommandByDoubleClick("${current.project.path}");
        commandsEditor.waitTextIntoEditor("echo ${current.project.path}");
        commandsEditor.clickOnRunButton();
        consoles.waitExpectedTextIntoConsole("/projects/" + PROJ_NAME);
    }

    @Test(priority = 2)
    public void checkCommandMacrosIntoPreviewUrl() {
        commandsEditor.cancelFormInEditorByEscape();
        commandsEditor.setFocusIntoTypeCommandsEditor(PREVIEW_URL_EDITOR);
        commandsEditor.setCursorToLine(1);
        commandsEditor.waitActiveEditor();
        commandsEditor.selectMacrosLinkInCommandsEditor(PREVIEW_MACROS_LINK);
        commandsEditor.typeTextIntoSearchMacroField("server.22/");
        commandsEditor.waitTextIntoSearchMacroField("server.22/");
        String[] macrosItems = {"${server.22/tcp.hostname}", "${server.22/tcp.port}", "${server.22/tcp.protocol}",
                                "${server.22/tcp}", "Returns hostname of a server registered by name",
                                "Returns port of a server registered by name",
                                "Returns protocol of a server registered by name",
                                "Returns protocol, hostname and port of an internal server"};
        for (String macrosItem : macrosItems) {
            commandsEditor.waitTextIntoMacrosContainer(macrosItem);
        }
        commandsEditor.enterMacroCommandByEnter("${server.22/tcp.hostname}");
        commandsEditor.waitTextIntoEditor("${server.22/tcp.hostname}");
        commandsEditor.clickOnRunButton();
        consoles.waitExpectedTextIntoPreviewUrl("ssh");
        commandsEditor.setFocusIntoTypeCommandsEditor(PREVIEW_URL_EDITOR);
        commandsEditor.setCursorToLine(1);
        commandsEditor.selectLineAndDelete();
        commandsEditor.waitActiveEditor();
        commandsEditor.selectMacrosLinkInCommandsEditor(PREVIEW_MACROS_LINK);
        commandsEditor.selectMacroCommand("${server.22/tcp}");
        commandsEditor.typeTextIntoEditor(Keys.ARROW_UP.toString());
        commandsEditor.typeTextIntoEditor(Keys.SPACE.toString());
        commandsEditor.waitMacroCommandIsSelected("${server.22/tcp.protocol}");
        commandsEditor.enterMacroCommandByDoubleClick("${server.22/tcp.protocol}");
        commandsEditor.waitTextIntoEditor("${server.22/tcp.protocol}");
        commandsEditor.clickOnRunButton();
        consoles.waitExpectedTextIntoPreviewUrl("ssh");
    }

    private void createNewJavaCommand() {
        commandsExplorer.openCommandsExplorer();
        commandsExplorer.waitCommandExplorerIsOpened();
        commandsExplorer.clickAddCommandButton(RUN_GOAL);
        loader.waitOnClosed();
        commandsExplorer.chooseCommandTypeInContextMenu(JAVA_TYPE);
        loader.waitOnClosed();
        commandsExplorer.waitCommandInExplorerByName(JAVA_NAME);
        commandsEditor.waitTabIsPresent(JAVA_NAME);
    }

}
