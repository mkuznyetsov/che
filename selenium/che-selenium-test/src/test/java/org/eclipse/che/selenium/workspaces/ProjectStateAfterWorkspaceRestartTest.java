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
package org.eclipse.che.selenium.workspaces;

import org.eclipse.che.selenium.core.project.ProjectTemplates;
import com.google.inject.Inject;

import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestWorkspaceConstants;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URL;
import java.nio.file.Paths;

/**
 * @author Aleksandr Shmaraev
 *         on 10.03.16
 */
public class ProjectStateAfterWorkspaceRestartTest {
    private static final String  PROJECT_NAME         = NameGenerator.generate("Spring_Simple", 4);
    private static final String  EXP_TEXT_NOT_PRESENT = "@Override\n" + "   public ModelAndView handleRequest";
    private              boolean passedState          = false;

    @Inject
    private TestWorkspace            workspace;
    @Inject
    private DefaultTestUser          defaultTestUser;
    @Inject
    private Ide                      ide;
    @Inject
    private ProjectExplorer          projectExplorer;
    @Inject
    private Loader                   loader;
    @Inject
    private Consoles                 consoles;
    @Inject
    private ToastLoader              toastLoader;
    @Inject
    private Menu                     menu;
    @Inject
    private NotificationsPopupPanel  notificationsPanel;
    @Inject
    private CodenvyEditor            editor;
    @Inject
    private TestProjectServiceClient testProjectServiceClient;

    @BeforeClass
    public void setUp() throws Exception {
        URL resource = ProjectStateAfterWorkspaceRestartTest.this.getClass().getResource("/projects/guess-project");
        testProjectServiceClient.importProject(workspace.getId(), defaultTestUser.getAuthToken(), Paths.get(resource.toURI()),
                                               PROJECT_NAME,
                                               ProjectTemplates.MAVEN_SPRING
        );
        ide.open(workspace);
    }

    @Test
    public void checkProjectAfterStopStartWs() {
        // create workspace from dashboard
        projectExplorer.waitProjectExplorer();
        projectExplorer.waitItem(PROJECT_NAME);
        loader.waitOnClosed();
        projectExplorer.quickExpandWithJavaScript();
        projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/webapp/index.jsp");
        editor.waitActiveEditor();
        projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java");
        editor.waitActiveEditor();
        notificationsPanel.waitProgressPopupPanelClose();
        loader.waitOnClosed();

        // stop and start workspace
        menu.runCommand(TestMenuCommandsConstants.Workspace.WORKSPACE, TestMenuCommandsConstants.Workspace.STOP_WORKSPACE);
        loader.waitOnClosed();
        toastLoader.waitToastLoaderIsOpen();
        toastLoader.waitExpectedTextInToastLoader("Workspace is not running");
        loader.waitOnClosed();
        notificationsPanel.waitPopUpPanelsIsClosed();
        consoles.closeProcessesArea();
        editor.waitTabIsNotPresent("AppController");
        editor.waitTabIsNotPresent("index.jsp");
        //projectExplorer.clickOnProjectExplorerTab();
        projectExplorer.waitProjectExplorer();
        projectExplorer.waitDisappearItemByPath(PROJECT_NAME);
        toastLoader.waitExpectedTextInToastLoader("Workspace is not running");
        toastLoader.clickOnStartButton();

        // check state of the project
        notificationsPanel.waitExpectedMessageOnProgressPanelAndClosed(TestWorkspaceConstants.RUNNING_WORKSPACE_MESS);
        projectExplorer.waitProjectExplorer();
        toastLoader.waitToastLoaderIsClosed();
        loader.waitOnClosed();
        projectExplorer.waitItem(PROJECT_NAME);
        projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples");
        editor.waitTabIsPresent("AppController");
        editor.waitTabIsPresent("index.jsp");
        projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java");
        projectExplorer.waitItem(PROJECT_NAME + "/src/main/webapp/index.jsp");
        projectExplorer.openItemByPath(PROJECT_NAME + "/README.md");
        editor.waitActiveEditor();
        editor.waitTextNotPresentIntoEditor(EXP_TEXT_NOT_PRESENT);
        editor.waitTextIntoEditor("Developer Workspace");
    }
}
