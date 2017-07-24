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
package org.eclipse.che.selenium.core.workspace;

import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.selenium.core.user.TestUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.workspace.MemoryMeasure.GB;

/**
 * @author Anatolii Bazko
 */
public class TestWorkspaceImpl implements TestWorkspace {
    private static final Logger LOG = LoggerFactory.getLogger(TestWorkspaceImpl.class);

    private final String                     name;
    private final CompletableFuture<Void>    future;
    private final TestUser                   owner;
    private final AtomicReference<String>    id;
    private final TestWorkspaceServiceClient workspaceServiceClient;

    public TestWorkspaceImpl(String name,
                             TestUser owner,
                             int memoryInGB,
                             String template,
                             TestWorkspaceServiceClient workspaceServiceClient) {
        this.name = name;
        this.owner = owner;
        this.id = new AtomicReference<>();
        this.workspaceServiceClient = workspaceServiceClient;

        this.future = CompletableFuture.runAsync(() -> {
            URL resource = TestWorkspaceImpl.class.getResource("/templates/workspace/" + template);
            if (resource == null) {
                throw new IllegalStateException(format("Workspace template '%s' not found", template));
            }

            try {
                final Workspace ws = workspaceServiceClient.createWorkspace(name, memoryInGB, GB, resource.getPath(), owner.getAuthToken());
                workspaceServiceClient.start(id.updateAndGet((s) -> ws.getId()), name, owner);

                LOG.info("Workspace name='{}' id='{}' has been created.", name, ws.getId());

            } catch (Exception e) {
                String errorMessage = format("Workspace name='%s' start failed.", name);
                LOG.error(errorMessage, e);

                try {
                    workspaceServiceClient.delete(name, owner.getName(), owner.getAuthToken());
                } catch (Exception e1) {
                    throw new IllegalStateException(format("Failed to remove workspace name='%s' when start is failed.", name), e);
                }

                throw new IllegalStateException(errorMessage, e);
            }
        });
    }

    @Override
    public void await() throws InterruptedException, ExecutionException {
        future.get();
    }

    @Override
    public String getName() throws ExecutionException, InterruptedException {
        return future.thenApply(aVoid -> name).get();
    }

    @Override
    public String getId() throws ExecutionException, InterruptedException {
        return future.thenApply(aVoid -> id.get()).get();
    }

    @Override
    public TestUser getOwner() {
        return owner;
    }

    @PreDestroy
    @Override
    public void delete() {
        future.thenAccept(aVoid -> {
            try {
                workspaceServiceClient.delete(name, owner.getName(), owner.getAuthToken());
                LOG.info("Workspace name='{}', id='{}' removed", name, getId());
            } catch (Exception e) {
                throw new RuntimeException(format("Failed to remove workspace '%s'", this), e);
            }
        });
    }
}
