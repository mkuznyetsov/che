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
package org.eclipse.che.selenium.editor.autocomplete;

import org.eclipse.che.selenium.core.project.ProjectTemplates;
import com.google.inject.Inject;

import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.MavenPluginStatusBar;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Igor Vinokur
 * @author Aleksandr Shmaraev
 */
public class OpenDeclarationTest {
    private static final String PROJECT_NAME = NameGenerator.generate(OpenDeclarationTest.class.getSimpleName(), 4);

    private String expectedTextBeforeDownloadSources = "";
    private String expectedTextAfterDownloadSources  = "";

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
    private CodenvyEditor            editor;
    @Inject
    private MavenPluginStatusBar     mavenPluginStatusBar;
    @Inject
    private AskForValueDialog        askForValueDialog;
    @Inject
    private Events                   events;
    @Inject
    private TestProjectServiceClient testProjectServiceClient;

    @BeforeClass
    public void prepare() throws Exception {
        URL resources = OpenDeclarationTest.class.getResource("expected-test-before-download-sources");
        List<String> expectedBeforeTextList = Files.readAllLines(Paths.get(resources.toURI()), Charset.forName("UTF-8"));
        for (String bufer : expectedBeforeTextList) {
            expectedTextBeforeDownloadSources += bufer + '\n';
        }

        resources = OpenDeclarationTest.class.getResource("expected-test-after-download-sources");
        List<String> expectedAfterTextList = Files.readAllLines(Paths.get(resources.toURI()), Charset.forName("UTF-8"));
        for (String bufer : expectedAfterTextList) {
            expectedTextAfterDownloadSources += bufer + '\n';
        }

        URL resource = getClass().getResource("/projects/default-spring-project");
        testProjectServiceClient.importProject(workspace.getId(), defaultTestUser.getAuthToken(), Paths.get(resource.toURI()),
                                               PROJECT_NAME,
                                               ProjectTemplates.MAVEN_SPRING
        );
        ide.open(workspace);
    }

    @Test
    public void navigateToSourceTest() throws Exception {
        projectExplorer.waitItem(PROJECT_NAME);
        events.clickProjectEventsTab();
        mavenPluginStatusBar.waitClosingInfoPanel();
        projectExplorer.quickExpandWithJavaScript();
        projectExplorer.openItemByVisibleNameInExplorer("AppController.java");
        loader.waitOnClosed();
        editor.selectTabByName("AppController");
        editor.setCursorToLine(20);
        editor.typeTextIntoEditor(Keys.ENTER.toString());
        editor.typeTextIntoEditor("import sun.net.spi.nameservice.dns.DNSNameServiceDescriptor;");
        editor.typeTextIntoEditor(Keys.ENTER.toString());
        editor.setCursorToLine(25);
        editor.typeTextIntoEditor(Keys.ENTER.toString());
        editor.typeTextIntoEditor("DNSNameServiceDescriptor descriptor = new DNSNameServiceDescriptor();");
        editor.typeTextIntoEditor(Keys.ENTER.toString());
        editor.typeTextIntoEditor("String sdf = descriptor.getProviderName();");
        editor.typeTextIntoEditor(Keys.ENTER.toString());
        loader.waitOnClosed();

        editor.setCursorToDefinedLineAndChar(26, 10);
        editor.typeTextIntoEditor(Keys.F4.toString());
        editor.waitTabIsPresent("DNSNameServiceDescriptor");
        editor.waitActiveEditor();
        editor.setCursorToLine(5);
        editor.waitTextElementsActiveLine("DNSNameServiceDescriptor");
        editor.closeFileByNameWithSaving("DNSNameServiceDescriptor");
        editor.waitTabIsNotPresent("DNSNameServiceDescriptor");
        editor.selectTabByName("AppController");
        editor.setCursorToDefinedLineAndChar(27, 39);
        editor.typeTextIntoEditor(Keys.F4.toString());
        editor.waitTabIsPresent("DNSNameServiceDescriptor");
        editor.setCursorToLine(11);
        editor.waitTextElementsActiveLine("getProviderName");
        editor.closeFileByNameWithSaving("DNSNameServiceDescriptor");

        // check an ability to download source
        editor.selectTabByName("AppController");
        editor.setCursorToDefinedLineAndChar(30, 12);
        editor.typeTextIntoEditor(Keys.F4.toString());
        editor.waitTabIsPresent("ModelAndView");
        //editor.waitTextIntoEditor(expectedTextBeforeDownloadSources);
        editor.clickOnDownloadSourcesLink();
        loader.waitOnClosed();
        //editor.waitTextIntoEditor(expectedTextAfterDownloadSources);

        editor.closeFileByNameWithSaving("ModelAndView");

        // check go to class
        editor.selectTabByName("AppController");
        editor.setCursorToDefinedLineAndChar(30, 12);
        editor.typeTextIntoEditor(Keys.F4.toString());
        editor.waitTabIsPresent("ModelAndView");
        editor.waitTextElementsActiveLine("ModelAndView");
        editor.waitSpecifiedValueForLineAndChar(44, 14);
        editor.closeFileByNameWithSaving("ModelAndView");

        //Check go to method
        editor.selectTabByName("AppController");
        editor.setCursorToDefinedLineAndChar(43, 16);
        editor.typeTextIntoEditor(Keys.F4.toString());
        editor.waitTabIsPresent("ModelAndView");
        editor.waitTextElementsActiveLine("addObject");
        editor.waitSpecifiedValueForLineAndChar(226, 22);

        //Check go to inner method
        editor.setCursorToDefinedLineAndChar(227, 9);
        editor.typeTextIntoEditor(Keys.F4.toString());
        editor.waitTabIsPresent("ModelAndView");
        editor.waitTextElementsActiveLine("getModelMap()");
        editor.waitSpecifiedValueForLineAndChar(203, 18);
    }
}
