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
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.ConfigureClasspath;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.openqa.selenium.Keys;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkersType.ERROR_MARKER;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.BUILD_PATH;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.SubMenuBuildPath.CONFIGURE_CLASSPATH;

/**
 * @author Aleksandr Shmaraev
 */
public class PlainJavaProjectConfigureClasspathTest {
    private static final String PROJECT_NAME     = NameGenerator.generate("PlainJava-", 4);
    private static final String LIB_PROJECT      = "lib";

    private static final List<String> listJar = Arrays.asList(
            "rt.jar - /opt/jdk1.8.0_45/jre/lib", "cldrdata.jar - /opt/jdk1.8.0_45/jre/lib/ext",
            "dnsns.jar - /opt/jdk1.8.0_45/jre/lib/ext", "jfxrt.jar - /opt/jdk1.8.0_45/jre/lib/ext",
            "localedata.jar - /opt/jdk1.8.0_45/jre/lib/ext", "nashorn.jar - /opt/jdk1.8.0_45/jre/lib/ext",
            "sunec.jar - /opt/jdk1.8.0_45/jre/lib/ext", "sunjce_provider.jar - /opt/jdk1.8.0_45/jre/lib/ext",
            "sunpkcs11.jar - /opt/jdk1.8.0_45/jre/lib/ext ", "zipfs.jar - /opt/jdk1.8.0_45/jre/lib/ext");

    @Inject
    private TestWorkspace            ws;
    @Inject
    private Ide                      ide;
    @Inject
    private DefaultTestUser          productUser;
    @Inject
    private ProjectExplorer          projectExplorer;
    @Inject
    private ConfigureClasspath       configureClasspath;
    @Inject
    private CodenvyEditor            codenvyEditor;
    @Inject
    private Loader                   loader;
    @Inject
    private Menu                     menu;
    @Inject
    private TestProjectServiceClient testProjectServiceClient;

    @BeforeClass
    public void prepare() throws Exception {
        URL resource = getClass().getResource("/projects/simple-java-project");
        testProjectServiceClient.importProject(ws.getId(), productUser.getAuthToken(), Paths.get(resource.toURI()), PROJECT_NAME,
                                               ProjectTemplates.PLAIN_JAVA
        );

        resource = getClass().getResource("/projects/lib");
        testProjectServiceClient.importProject(ws.getId(), productUser.getAuthToken(), Paths.get(resource.toURI()), LIB_PROJECT,
                                               ProjectTemplates.PLAIN_JAVA
        );
        ide.open(ws);
    }

    @Test
    public void checkConfigureClasspathPlainJavaProject() {
        projectExplorer.waitProjectExplorer();
        projectExplorer.waitItem(PROJECT_NAME);
        projectExplorer.waitItem(LIB_PROJECT);
        projectExplorer.openItemByPath(PROJECT_NAME);
        projectExplorer.waitItem(PROJECT_NAME + "/bin");
        projectExplorer.waitItem(PROJECT_NAME + "/src");
        projectExplorer.waitItem(PROJECT_NAME + "/test");

        // check build path to the subfolder 'java' from context menu
        projectExplorer.openItemByPath(PROJECT_NAME + "/test");
        projectExplorer.openItemByPath(PROJECT_NAME + "/test/java");
        projectExplorer.openItemByPath(PROJECT_NAME + "/test/java/com");
        projectExplorer.openItemByPath(PROJECT_NAME + "/test/java/com/company");
        projectExplorer.selectItem(PROJECT_NAME + "/test/java");
        projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + "/test/java");
        projectExplorer.clickOnItemInContextMenu(BUILD_PATH);
        projectExplorer.clickOnItemInContextMenu(
                TestProjectExplorerContextMenuConstants.SubMenuBuildPath.USE_AS_SOURCE_FOLDER);
        projectExplorer.waitItem(PROJECT_NAME + "/test/java/com/company");
        projectExplorer.selectItem(PROJECT_NAME + "/test/java");
        projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + "/test/java");
        projectExplorer.clickOnItemInContextMenu(BUILD_PATH);
        loader.waitOnClosed();
        projectExplorer.clickOnItemInContextMenu(
                TestProjectExplorerContextMenuConstants.SubMenuBuildPath.UNMARK_AS_SOURCE_FOLDER);
        projectExplorer.waitDisappearItemByPath(PROJECT_NAME + "/test/java/com/company");

        // check build path to the folder 'test' from context menu
        projectExplorer.selectItem(PROJECT_NAME + "/test");
        projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + "/test");
        projectExplorer.clickOnItemInContextMenu(BUILD_PATH);
        projectExplorer.clickOnItemInContextMenu(
                TestProjectExplorerContextMenuConstants.SubMenuBuildPath.USE_AS_SOURCE_FOLDER);
        projectExplorer.waitItem(PROJECT_NAME + "/test/java/com/company");
        projectExplorer.selectItem(PROJECT_NAME + "/test");
        projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME + "/test");
        projectExplorer.clickOnItemInContextMenu(BUILD_PATH);
        loader.waitOnClosed();
        projectExplorer.clickOnItemInContextMenu(
                TestProjectExplorerContextMenuConstants.SubMenuBuildPath.UNMARK_AS_SOURCE_FOLDER);
        projectExplorer.waitDisappearItemByPath(PROJECT_NAME + "/test/java/com/company");

        // check the 'Cancel' button of the 'Select Path' form
        projectExplorer.selectItem(PROJECT_NAME);
        menu.runCommand(TestMenuCommandsConstants.Project.PROJECT, TestMenuCommandsConstants.Project.CONFIGURE_CLASSPATH);
        configureClasspath.waitConfigureClasspathFormIsOpen();
        configureClasspath.selectSourceCategory();
        configureClasspath.addJarOrFolderToBuildPath(ConfigureClasspath.ADD_FOLDER);
        configureClasspath.waitSelectPathFormIsOpen();
        configureClasspath.clickCancelBtnSelectPathForm();

        // check adding and deleting of a source folder
        configureClasspath.waitExpectedTextJarsAndFolderArea("/" + PROJECT_NAME + "/src");
        configureClasspath.addJarOrFolderToBuildPath(ConfigureClasspath.ADD_FOLDER);
        configureClasspath.waitSelectPathFormIsOpen();
        configureClasspath.openItemInSelectPathForm(PROJECT_NAME);
        configureClasspath.waitItemInSelectPathForm("bin");
        configureClasspath.waitItemInSelectPathForm("src");
        configureClasspath.waitItemInSelectPathForm("test");
        configureClasspath.selectItemInSelectPathForm("test");
        configureClasspath.clickOkBtnSelectPathForm();
        configureClasspath.waitExpectedTextJarsAndFolderArea("/" + PROJECT_NAME + "/test");
        configureClasspath.clickOnDoneBtnConfigureClasspath();
        projectExplorer.waitItem(PROJECT_NAME + "/test/java/com/company");
        projectExplorer.selectItem(PROJECT_NAME);
        projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME);
        projectExplorer.clickOnItemInContextMenu(BUILD_PATH);
        projectExplorer.clickOnItemInContextMenu(CONFIGURE_CLASSPATH);
        configureClasspath.waitConfigureClasspathFormIsOpen();
        configureClasspath.selectSourceCategory();
        configureClasspath.deleteJarOrFolderFromBuildPath("/" + PROJECT_NAME + "/test");
        configureClasspath.waitExpectedTextIsNotPresentInJarsAndFolderArea("/" + PROJECT_NAME + "/test");
        configureClasspath.clickOnDoneBtnConfigureClasspath();
        projectExplorer.openItemByPath(PROJECT_NAME + "/test");
        projectExplorer.waitDisappearItemByPath(PROJECT_NAME + "/test/java/com/company");

        // check the library container
        projectExplorer.selectItem(PROJECT_NAME);
        menu.runCommand(TestMenuCommandsConstants.Project.PROJECT, TestMenuCommandsConstants.Project.CONFIGURE_CLASSPATH);
        configureClasspath.waitConfigureClasspathFormIsOpen();
        configureClasspath.clickLibraryContainer("org.eclipse.jdt.launching.JRE_CONTAINER");
        for (String jarName : listJar) {
            Assert.assertTrue(configureClasspath.getTextFromJarsAndFolderArea().contains(jarName));
        }
        configureClasspath.clickLibraryContainer("org.eclipse.jdt.launching.JRE_CONTAINER");
        configureClasspath.waitExpectedTextIsNotPresentInJarsAndFolderArea("rt.jar - /opt/jdk1.8.0_45/jre/lib");

        // check adding jar file
        configureClasspath.addJarOrFolderToBuildPath(ConfigureClasspath.ADD_JAR);
        configureClasspath.waitSelectPathFormIsOpen();
        configureClasspath.openItemInSelectPathForm(LIB_PROJECT);
        configureClasspath.selectItemInSelectPathForm("log4j-1.2.17.jar");
        configureClasspath.clickOkBtnSelectPathForm();
        configureClasspath.waitExpectedTextJarsAndFolderArea("log4j-1.2.17.jar - /projects/lib");
        configureClasspath.deleteJarOrFolderFromBuildPath("log4j-1.2.17.jar - /projects/lib");
        configureClasspath.waitExpectedTextIsNotPresentInJarsAndFolderArea("log4j-1.2.17.jar - /projects/lib");
        configureClasspath.addJarOrFolderToBuildPath(ConfigureClasspath.ADD_JAR);
        configureClasspath.waitSelectPathFormIsOpen();
        configureClasspath.openItemInSelectPathForm(LIB_PROJECT);
        configureClasspath.selectItemInSelectPathForm("mockito-all-1.9.5.jar");
        configureClasspath.clickOkBtnSelectPathForm();
        configureClasspath.waitExpectedTextJarsAndFolderArea("mockito-all-1.9.5.jar - /projects/lib");
        configureClasspath.clickOnDoneBtnConfigureClasspath();
        projectExplorer.openItemByPath(PROJECT_NAME + "/src");
        projectExplorer.openItemByPath(PROJECT_NAME + "/src/com/company");
        projectExplorer.openItemByPath(PROJECT_NAME + "/src/com/company/Main.java");
        codenvyEditor.waitActiveEditor();
        codenvyEditor.setCursorToLine(17);
        codenvyEditor.typeTextIntoEditor(Keys.ENTER.toString());
        loader.waitOnClosed();
        codenvyEditor.setCursorToLine(17);
        codenvyEditor.typeTextIntoEditor(Keys.TAB.toString());
        codenvyEditor.typeTextIntoEditor("Mockito mockito = new Mockito();");
        codenvyEditor.waitTextIntoEditor("Mockito mockito = new Mockito();");
        codenvyEditor.waitMarkerInPosition(ERROR_MARKER, 17);
        codenvyEditor.launchPropositionAssistPanel();
        codenvyEditor.enterTextIntoFixErrorPropByDoubleClick("Import 'Mockito' (org.mockito)");
        codenvyEditor.waitErrorPropositionPanelClosed();
        codenvyEditor.waitTextIntoEditor("import org.mockito.Mockito;");
        codenvyEditor.setCursorToLine(19);
        codenvyEditor.waitMarkerDisappears(ERROR_MARKER, 19);
    }

}
