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
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URL;
import java.nio.file.Paths;

/**
 * @author Andrienko Alexander
 * @author Andrey Chizhikov
 */
public class CreateNewNotJavaFilesFromContextMenuTest {

    private static final String PROJECT_NAME                   = "FileCreation1";
    private static final String NEW_FILE_NAME                  = "file.txt";
    private static final String NEW_XML_FILE                   = "NewXml";
    private static final String NEW_LESS_FILE                  = "NewLess";
    private static final String NEW_CSS_FILE                   = "NewCSS";
    private static final String NEW_HTML_FILE                  = "NewHTML";
    private static final String NEW_JS_FILE                    = "NewJS";
    private static final String DEFAULT_TEXT_FOR_NEW_FILE_NAME = "";
    private static final String DEFAULT_TEXT_FOR_NEW_XML_FILE  = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    private static final String DEFAULT_TEXT_FOR_NEW_LESS_FILE = "@CHARSET \"UTF-8\"\n;";
    private static final String DEFAULT_TEXT_FOR_NEW_CSS_FILE  = "@CHARSET \"UTF-8\";";
    private static final String DEFAULT_TEXT_FOR_NEW_HTML_FILE = "<!DOCTYPE html>\n" +
                                                                 "<html>\n" +
                                                                 "<head>\n" +
                                                                 "    <title></title>\n" +
                                                                 "</head>\n" +
                                                                 "<body>\n" +
                                                                 "\n" +
                                                                 "</body>\n" +
                                                                 "</html>";
    private static final String PATH_TO_FILES                  = PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples";

    @Inject
    private TestWorkspace           testWorkspace;
    @Inject
    private DefaultTestUser          defaultTestUser;
    @Inject
    private Ide                      ide;
    @Inject
    private ProjectExplorer          projectExplorer;
    @Inject
    private Loader                   loader;
    @Inject
    private CodenvyEditor            editor;
    @Inject
    private NotificationsPopupPanel  notificationsPopupPanel;
    @Inject
    private AskForValueDialog        askForValueDialog;
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
    public void createNewFileFromContextMenuTest() throws Exception {
        projectExplorer.waitProjectExplorer();
        projectExplorer.waitItem(PROJECT_NAME);
        notificationsPopupPanel.waitProgressPopupPanelClose();
        projectExplorer.quickExpandWithJavaScript();
        projectExplorer.openItemByVisibleNameInExplorer("AppController.java");
        loader.waitOnClosed();

        //create new file
        createNewFile(NEW_FILE_NAME,
                      TestProjectExplorerContextMenuConstants.SubMenuNew.FILE,
                      "");
        checkDefaultTextInCodeMirrorEditorForFile(DEFAULT_TEXT_FOR_NEW_FILE_NAME, NEW_FILE_NAME);
        editor.closeFileByNameWithSaving(NEW_FILE_NAME);

        //create new xml file
        createNewFile(NEW_XML_FILE,
                      TestProjectExplorerContextMenuConstants.SubMenuNew.XML_FILE,
                      ".xml");
        checkDefaultTextInCodeMirrorEditorForFile(DEFAULT_TEXT_FOR_NEW_XML_FILE, NEW_XML_FILE + ".xml");
        editor.closeFileByNameWithSaving(NEW_XML_FILE + ".xml");

        //create new less file
        createNewFile(NEW_LESS_FILE,
                      TestProjectExplorerContextMenuConstants.SubMenuNew.LESS_FILE,
                      ".less");
        checkDefaultTextInCodeMirrorEditorForFile(DEFAULT_TEXT_FOR_NEW_LESS_FILE, NEW_LESS_FILE + ".less");
        editor.closeFileByNameWithSaving(NEW_LESS_FILE + ".less");

        //create new css file
        createNewFile(NEW_CSS_FILE,
                      TestProjectExplorerContextMenuConstants.SubMenuNew.CSS_FILE,
                      ".css");
        checkDefaultTextInCodeMirrorEditorForFile(DEFAULT_TEXT_FOR_NEW_CSS_FILE, NEW_CSS_FILE + ".css");
        editor.closeFileByNameWithSaving(NEW_CSS_FILE + ".css");

        //create new html file
        createNewFile(NEW_HTML_FILE,
                      TestProjectExplorerContextMenuConstants.SubMenuNew.HTML_FILE,
                      ".html");
        checkDefaultTextInCodeMirrorEditorForFile(DEFAULT_TEXT_FOR_NEW_HTML_FILE, NEW_HTML_FILE + ".html");
        editor.closeFileByNameWithSaving(NEW_HTML_FILE + ".html");


        //create new js file
        createNewFile(NEW_JS_FILE,
                      TestProjectExplorerContextMenuConstants.SubMenuNew.JAVASCRIPT_FILE,
                      ".js");
        editor.closeFileByNameWithSaving(NEW_JS_FILE + ".js");
    }

    public void createNewFile(String name,
                              String type,
                              String fileExt) throws Exception {
        projectExplorer.selectItem(PATH_TO_FILES);

        //create new File from context menu
        projectExplorer.openContextMenuByPathSelectedItem(PATH_TO_FILES);
        projectExplorer.clickOnItemInContextMenu(TestProjectExplorerContextMenuConstants.NEW);
        projectExplorer.clickOnItemInContextMenu(type);
        askForValueDialog.typeAndWaitText(name);
        askForValueDialog.clickOkBtn();
        loader.waitOnClosed();
        projectExplorer.waitItemInVisibleArea(name + fileExt);
    }

    public void checkDefaultTextInCodeMirrorEditorForFile(String defaultText, String fileName) throws InterruptedException {
        editor.waitActiveEditor();
        editor.waitTabIsPresent(fileName);
        editor.waitTextIntoEditor(defaultText);
    }
}
