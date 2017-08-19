/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.pageobject;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.everrest.core.impl.provider.json.JsonValue;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Andrey Chizhikov */
@Singleton
public class Swagger {

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public Swagger(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String WORKSPACE = "//a[@href='#!/workspace']";
    String PROJECT = "//a[@href='#!/project']";

    String GET_PROJECT = "//a[@href='#!/project/getProjects']";
    String GET_PROJECT_TRY_IT_OUT = "//li[@id='resource_project']//input[@value='Try it out!']";
    String GET_WS_ID_RESPONSE_BODY = "//li[@id='resource_project']//code";

    String GET_WORKSPACES = "//a[@href='#!/workspace/getWorkspaces']";
    String TRY_IT_OUT = "//li[@id='workspace_getWorkspaces']//input[@value='Try it out!']";
    String RESPONSE_BODY = "//li[@id='workspace_getWorkspaces']//code";
    String TRY_IT_OUT_RUNTIME = "//li[@id='workspace_getWorkspaces']//input[@value='Try it out!']";
    String RESPONSE_BODY_RUNTIME = "//li[@id='workspace_getWorkspaces']//code";
    String BASE_URL_INPUT = "//input[@id='input_baseUrl']";
    String EXPLORE = "//a[@id='explore']";
  }

  @FindBy(xpath = Locators.WORKSPACE)
  WebElement workSpaceLink;

  /** expand 'workspace' item */
  private void expandWorkSpaceItem() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(workSpaceLink))
        .click();
  }

  /** collapse 'workspace' item */
  private void collapseWorkSpaceItem() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(workSpaceLink))
        .click();
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id("workspace_endpoint_list")));
  }

  /**
   * Clicks on the element by Xpath
   *
   * @param xPath web element Xpath
   */
  private void clickElementByXpath(String xPath) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)))
        .click();
  }

  /**
   * Clicks on the 'Try it out' by Xpath
   *
   * @param xPath web element Xpath
   */
  private void clickTryItOutByXpath(String xPath) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(By.xpath(xPath)))
        .click();
  }

  /**
   * get wsId from the text that has been graped from the swagger page
   *
   * @return result search by key
   */
  public String getWsNameFromWorkspacePage() {
    expandWorkSpaceItem();
    clickElementByXpath(Locators.GET_WORKSPACES);
    clickTryItOutByXpath(Locators.TRY_IT_OUT);
    String json =
        new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
            .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.RESPONSE_BODY)))
            .getText();
    List<WorkspaceDto> workspaces =
        DtoFactory.getInstance().createListDtoFromJson(json, WorkspaceDto.class);
    return workspaces.get(0).getConfig().getName();
  }

  /**
   * create URL
   *
   * @param port number of port
   * @param machineToken authorization token for machine
   * @param workspaceName name of workspace
   * @return String
   * @throws JsonParseException
   */
  public String createURLByExposedPort(String port, String machineToken, String workspaceName)
      throws JsonParseException {
    String json;
    String url;
    String serverAddress = "";
    expandWorkSpaceItem();
    clickTryItOutByXpath(Locators.TRY_IT_OUT_RUNTIME);
    json =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.xpath(Locators.RESPONSE_BODY_RUNTIME)))
            .getText();
    JsonValue jsonValue = JsonHelper.parseJson(json);
    JsonValue foundValue = null;
    Iterator<JsonValue> jsonIterator = jsonValue.getElements();
    while (jsonIterator.hasNext()) {
      JsonValue value = jsonIterator.next();
      JsonValue config = value.getElement("config");
      JsonValue defaultEnv = config.getElement("defaultEnv");
      if (defaultEnv.isString() && workspaceName.equals(defaultEnv.getStringValue())) {
        foundValue = value;
        break;
      }
    }
    if (foundValue != null) {
      serverAddress =
          foundValue
              .getElement("runtime")
              .getElement("devMachine")
              .getElement("runtime")
              .getElement("servers")
              .getElement(port)
              .getElement("url")
              .toString()
              .replaceAll("\"", "");
    }

    url = serverAddress + "/docs/swagger.json?token=" + machineToken;
    collapseWorkSpaceItem();
    return url;
  }

  /** click on 'project' link */
  private void clickOnProjectLink() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(By.xpath(Locators.PROJECT)))
        .click();
  }

  /**
   * enter URL and click 'Explore'
   *
   * @param URL URL
   */
  private void enterURLAndClickExplore(String URL) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.BASE_URL_INPUT)))
        .clear();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.BASE_URL_INPUT)))
        .sendKeys(URL);
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.EXPLORE)))
        .click();
  }

  /**
   * @param URL URL
   * @return return list names of projects from workspace
   * @throws JsonParseException
   */
  public List<String> getNamesOfProjects(String URL) throws JsonParseException {
    String json;
    List<String> projectsList = new ArrayList<>();
    enterURLAndClickExplore(URL);
    clickOnProjectLink();
    clickElementByXpath(Locators.GET_PROJECT);
    clickTryItOutByXpath(Locators.GET_PROJECT_TRY_IT_OUT);
    WaitUtils.sleepQuietly(2);
    json =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.xpath(Locators.GET_WS_ID_RESPONSE_BODY)))
            .getText();
    Iterator<JsonValue> jsonHelperIterator = JsonHelper.parseJson(json).getElements();

    while (jsonHelperIterator.hasNext()) {
      projectsList.add(
          jsonHelperIterator.next().getElement("name").toString().replaceAll("\"", ""));
    }

    return projectsList;
  }
}
