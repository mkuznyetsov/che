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
package org.eclipse.che.selenium.filewatcher;

import org.eclipse.che.selenium.core.project.ProjectTemplates;
import com.google.inject.Inject;

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.pageobject.InjectPageObject;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URL;
import java.nio.file.Paths;


/**
 * @author Musienko Maxim
 */
public class UpdateFilesWithoutIDE {

    private final static String PROJECT_NAME = NameGenerator.generate("project", 6);

    @Inject
    private TestWorkspace   ws;
    @Inject
    private DefaultTestUser user;

    @InjectPageObject(driverId = 1)
    private Ide ide1;

    @InjectPageObject(driverId = 1)
    private ProjectExplorer projectExplorer1;

    @InjectPageObject(driverId = 1)
    private CodenvyEditor editor1;

    @InjectPageObject(driverId = 1)
    private NotificationsPopupPanel notifications1;


    @InjectPageObject(driverId = 2)
    private Ide ide2;

    @InjectPageObject(driverId = 2)
    private CodenvyEditor editor2;

    @InjectPageObject(driverId = 2)
    private NotificationsPopupPanel notifications2;

    @InjectPageObject(driverId = 2)
    private ProjectExplorer projectExplorer2;

    @Inject
    private TestWorkspaceServiceClient workspaceServiceClient;

    @Inject
    private TestProjectServiceClient testProjectServiceClient;

    @BeforeClass
    public void setUp() throws Exception {
        URL resource = getClass().getResource("/projects/spring-project-for-file-watcher-tabs");
        testProjectServiceClient.importProject(ws.getId(), user.getAuthToken(), Paths.get(resource.toURI()),
                                               PROJECT_NAME,
                                               ProjectTemplates.MAVEN_SPRING
        );

        ide1.open(ws);
        ide2.open(ws);
    }

    @Test
    public void checkEditingFileWithoutIDE() throws Exception {
        String nameFiletxt2 = "file2.txt";
        String nameFiletxt3 = "file3.txt";
        String expectedMessage2 = "File '" + nameFiletxt2 + "' is updated";
        String expectedMessage3 = "File '" + nameFiletxt3 + "' is updated";
        projectExplorer1.openItemByPath(PROJECT_NAME);
        projectExplorer2.openItemByPath(PROJECT_NAME);
        projectExplorer1.openItemByPath(PROJECT_NAME + "/" + nameFiletxt2);
        editor1.waitActiveEditor();
        projectExplorer2.openItemByPath(PROJECT_NAME + "/" + nameFiletxt2);
        editor1.waitActiveEditor();

        Workspace workspace = workspaceServiceClient.getByName(ws.getName(), user.getName(), user.getAuthToken());
        testProjectServiceClient.updateFile(workspace.getId(), user.getAuthToken(),
                                            PROJECT_NAME + "/" + nameFiletxt2, Long.toString(System.currentTimeMillis())
        );

        notifications1.waitExpectedMessageOnProgressPanelAndClosed(expectedMessage2, 10);
        notifications2.waitExpectedMessageOnProgressPanelAndClosed(expectedMessage2, 10);
        projectExplorer1.openItemByPath(PROJECT_NAME + "/" + nameFiletxt3);
        editor1.waitActiveEditor();
        projectExplorer2.openItemByPath(PROJECT_NAME + "/" + nameFiletxt3);
        editor2.waitActiveEditor();

        workspace = workspaceServiceClient.getByName(ws.getName(), user.getName(), user.getAuthToken());
        testProjectServiceClient.updateFile(workspace.getId(), user.getAuthToken(),
                                            PROJECT_NAME + "/" + nameFiletxt3, Long.toString(System.currentTimeMillis())
        );

        notifications1.waitExpectedMessageOnProgressPanelAndClosed(expectedMessage3, 10);
        notifications2.waitExpectedMessageOnProgressPanelAndClosed(expectedMessage3, 10);
    }
}
