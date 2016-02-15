/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.machine.server;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineMetadataImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineRuntimeInfoImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.commons.test.SelfReturningAnswer;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Listeners(MockitoTestNGListener.class)
public class WsAgentLauncherImplTest {
    private static final String     WS_ID                         = "wsId";
    private static final String     MACHINE_ID                    = "machineId";
    private static final String     WS_AGENT_START_CMD_LINE       = "cmdLine";
    private static final String     API_ENDPOINT_PATH             = "/some/path/";
    private static final String     WS_AGENT_PORT                 = Integer.toString(WsAgentLauncherImpl.WS_AGENT_PORT);
    private static final long       WS_AGENT_MAX_START_TIME_MS    = 1000;
    private static final long       WS_AGENT_PING_DELAY_MS        = 1;
    private static final int        WS_AGENT_PING_CONN_TIMEOUT_MS = 1;
    private static final String     WS_AGENT_SERVER_LOCATION      = "ws-agent.com:456789";
    private static final String     WS_AGENT_SERVER_URL           = "http://" + WS_AGENT_SERVER_LOCATION;
    private static final ServerImpl SERVER                        = new ServerImpl("ref",
                                                                                   WS_AGENT_SERVER_LOCATION,
                                                                                   WS_AGENT_SERVER_URL);

    @Mock
    private MachineManager         machineManager;
    @Mock
    private HttpJsonRequestFactory requestFactory;
    @Mock
    private MachineImpl            machine;
    @Mock
    private HttpJsonResponse       pingResponse;
    @Mock
    private MachineMetadataImpl    machineMetadata;
    @Mock
    private MachineRuntimeInfoImpl machineRuntime;

    private HttpJsonRequest     pingRequest;
    private WsAgentLauncherImpl wsAgentLauncher;

    @BeforeMethod
    public void setUp() throws Exception {
        wsAgentLauncher = new WsAgentLauncherImpl(() -> machineManager,
                                                  requestFactory,
                                                  WS_AGENT_START_CMD_LINE,
                                                  new URI("http://localhost:8080" + API_ENDPOINT_PATH),
                                                  WS_AGENT_MAX_START_TIME_MS,
                                                  WS_AGENT_PING_DELAY_MS,
                                                  WS_AGENT_PING_CONN_TIMEOUT_MS);
        pingRequest = mock(HttpJsonRequest.class, new SelfReturningAnswer());
        when(machineManager.getDevMachine(WS_ID)).thenReturn(machine);
        when(machine.getId()).thenReturn(MACHINE_ID);
        when(machine.getRuntime()).thenReturn(machineRuntime);
        when(machineRuntime.getMetadata()).thenReturn(machineMetadata);
        doReturn(Collections.<String, Server>singletonMap(WS_AGENT_PORT, SERVER)).when(machineMetadata).getServers();
        when(requestFactory.fromUrl(anyString())).thenReturn(pingRequest);
        when(pingRequest.request()).thenReturn(pingResponse);
        when(pingResponse.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
    }

    @Test
    public void shouldStartWsAgentUsingMachineExec() throws Exception {
        wsAgentLauncher.startWsAgent(WS_ID);

        verify(machineManager).exec(eq(MACHINE_ID),
                                    eq(new CommandImpl(WsAgentLauncherImpl.WS_AGENT_PROCESS_NAME,
                                                       WS_AGENT_START_CMD_LINE,
                                                       "Arbitrary")),
                                    eq(WsAgentLauncherImpl.getWsAgentProcessOutputChannel(WS_ID)));

    }

    @Test
    public void shouldPingWsAgentAfterStart() throws Exception {
        wsAgentLauncher.startWsAgent(WS_ID);

        verify(requestFactory).fromUrl(UriBuilder.fromUri(WS_AGENT_SERVER_URL)
                                                 .replacePath(API_ENDPOINT_PATH)
                                                 .build()
                                                 .toString());
        verify(pingRequest).setMethod(HttpMethod.OPTIONS);
        verify(pingRequest).setTimeout(WS_AGENT_PING_CONN_TIMEOUT_MS);
        verify(pingRequest).request();
        verify(pingResponse).getResponseCode();
    }

    @Test
    public void shouldPingWsAgentMultipleTimesAfterStartIfPingFailsWithException() throws Exception {
        when(pingRequest.request()).thenThrow(new ServerException(""),
                                              new BadRequestException(""),
                                              new IOException())
                                   .thenReturn(pingResponse);

        wsAgentLauncher.startWsAgent(WS_ID);

        verify(requestFactory).fromUrl(UriBuilder.fromUri(WS_AGENT_SERVER_URL)
                                                 .replacePath(API_ENDPOINT_PATH)
                                                 .build()
                                                 .toString());
        verify(pingRequest).setMethod(HttpMethod.OPTIONS);
        verify(pingRequest).setTimeout(WS_AGENT_PING_CONN_TIMEOUT_MS);
        verify(pingRequest, times(4)).request();
        verify(pingResponse).getResponseCode();
    }

    @Test
    public void shouldPingWsAgentMultipleTimesAfterStartIfPingReturnsNotOKResponseCode() throws Exception {
        when(pingResponse.getResponseCode()).thenReturn(HttpURLConnection.HTTP_CREATED,
                                                        HttpURLConnection.HTTP_NO_CONTENT,
                                                        HttpURLConnection.HTTP_OK);

        wsAgentLauncher.startWsAgent(WS_ID);

        verify(requestFactory).fromUrl(UriBuilder.fromUri(WS_AGENT_SERVER_URL)
                                                 .replacePath(API_ENDPOINT_PATH)
                                                 .build()
                                                 .toString());
        verify(pingRequest).setMethod(HttpMethod.OPTIONS);
        verify(pingRequest).setTimeout(WS_AGENT_PING_CONN_TIMEOUT_MS);
        verify(pingRequest, times(3)).request();
        verify(pingResponse, times(3)).getResponseCode();
    }

    @Test
    public void shouldNotPingWsAgentAfterFirstSuccessfulPing() throws Exception {
        when(pingRequest.request()).thenThrow(new ServerException(""))
                                   .thenReturn(pingResponse);

        wsAgentLauncher.startWsAgent(WS_ID);

        verify(pingRequest, times(2)).request();
        verify(pingResponse).getResponseCode();
    }

    @Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp = "Test exception")
    public void shouldThrowNotFoundExceptionIfMachineManagerGetDevMachineForWsThrowsNotFoundException() throws Exception {
        final String notExistingWsId = "notExistingWsId";
        when(machineManager.getDevMachine(notExistingWsId)).thenThrow(new NotFoundException("Test exception"));

        wsAgentLauncher.startWsAgent(notExistingWsId);

        verify(machineManager).getDevMachine(eq(notExistingWsId));
    }

    @Test(expectedExceptions = MachineException.class, expectedExceptionsMessageRegExp = "Test exception")
    public void shouldThrowMachineExceptionIfMachineManagerGetDevMachineForWsThrowsMachineException() throws Exception {
        final String notExistingWsId = "notExistingWsId";
        when(machineManager.getDevMachine(notExistingWsId)).thenThrow(new MachineException("Test exception"));

        wsAgentLauncher.startWsAgent(notExistingWsId);

        verify(machineManager).getDevMachine(eq(notExistingWsId));
    }

    @Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp = "Test exception")
    public void shouldThrowNotFoundExceptionIfMachineManagerExecInDevMachineThrowsNotFoundException() throws Exception {
        when(machineManager.exec(anyString(), any(Command.class), anyString())).thenThrow(new NotFoundException("Test exception"));

        wsAgentLauncher.startWsAgent(WS_ID);

        verify(machineManager).exec(anyString(), any(Command.class), anyString());
    }

    @Test(expectedExceptions = MachineException.class, expectedExceptionsMessageRegExp = "Test exception")
    public void shouldThrowMachineExceptionIfMachineManagerExecInDevMachineThrowsMachineException() throws Exception {
        when(machineManager.exec(anyString(), any(Command.class), anyString())).thenThrow(new MachineException("Test exception"));

        wsAgentLauncher.startWsAgent(WS_ID);

        verify(machineManager).exec(anyString(), any(Command.class), anyString());
    }

    @Test(expectedExceptions = MachineException.class, expectedExceptionsMessageRegExp = "Test exception")
    public void shouldThrowMachineExceptionIfMachineManagerExecInDevMachineThrowsBadRequestException() throws Exception {
        when(machineManager.exec(anyString(), any(Command.class), anyString())).thenThrow(new BadRequestException("Test exception"));

        wsAgentLauncher.startWsAgent(WS_ID);

        verify(machineManager).exec(anyString(), any(Command.class), anyString());
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Workspace agent is not responding. Workspace " + WS_ID + " will be stopped")
    public void shouldThrowMachineExceptionIfPingsWereUnsuccessfulTooLong() throws Exception {
        when(pingRequest.request()).thenThrow(new ServerException(""));

        wsAgentLauncher.startWsAgent(WS_ID);
    }

    @Test
    public void shouldAddTrailingSlashToPingPath() throws Exception {
        String pingUrlWithoutTrailingSlash = WS_AGENT_SERVER_URL.substring(0, WS_AGENT_SERVER_URL.length() - 1);
        ServerImpl server = new ServerImpl(SERVER);
        server.setUrl(pingUrlWithoutTrailingSlash);
        doReturn(Collections.<String, Server>singletonMap(WS_AGENT_PORT, server)).when(machineMetadata).getServers();

        wsAgentLauncher.startWsAgent(WS_ID);

        verify(requestFactory).fromUrl(UriBuilder.fromUri(pingUrlWithoutTrailingSlash + "/")
                                                 .replacePath(API_ENDPOINT_PATH)
                                                 .build()
                                                 .toString());
        verify(pingRequest).setMethod(HttpMethod.OPTIONS);
        verify(pingRequest).setTimeout(WS_AGENT_PING_CONN_TIMEOUT_MS);
        verify(pingRequest).request();
        verify(pingResponse).getResponseCode();
    }
}