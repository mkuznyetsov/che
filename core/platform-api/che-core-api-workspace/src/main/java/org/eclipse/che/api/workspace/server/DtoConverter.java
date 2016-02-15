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
package org.eclipse.che.api.workspace.server;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Snapshot;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.RuntimeWorkspace;
import org.eclipse.che.api.core.model.workspace.UsersWorkspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.SnapshotDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.RuntimeWorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

// TODO! use global registry for DTO converters

/**
 * Helps to convert to/from DTOs related to workspace.
 *
 * @author Eugene Voevodin
 */
public final class DtoConverter {

    /**
     * Converts {@link UsersWorkspace} to {@link UsersWorkspaceDto}.
     */
    public static UsersWorkspaceDto asDto(UsersWorkspace workspace) {
        return newDto(UsersWorkspaceDto.class).withId(workspace.getId())
                                              .withStatus(workspace.getStatus())
                                              .withOwner(workspace.getOwner())
                                              .withTemporary(workspace.isTemporary())
                                              .withConfig(asDto(workspace.getConfig()));
    }

    /**
     * Converts {@link WorkspaceConfig} to {@link WorkspaceConfigDto}.
     */
    public static WorkspaceConfigDto asDto(WorkspaceConfig workspace) {
        final List<CommandDto> commands = workspace.getCommands()
                                                   .stream()
                                                   .map(DtoConverter::asDto)
                                                   .collect(toList());
        final List<ProjectConfigDto> projects = workspace.getProjects()
                                                         .stream()
                                                         .map(DtoConverter::asDto)
                                                         .collect(toList());
        final List<EnvironmentDto> environments = workspace.getEnvironments()
                                                           .stream()
                                                           .map(DtoConverter::asDto)
                                                           .collect(toList());

        return newDto(WorkspaceConfigDto.class).withName(workspace.getName())
                                               .withDefaultEnv(workspace.getDefaultEnv())
                                               .withCommands(commands)
                                               .withProjects(projects)
                                               .withEnvironments(environments)
                                               .withDescription(workspace.getDescription())
                                               .withAttributes(workspace.getAttributes());
    }

    /**
     * Converts {@link Command} to {@link CommandDto}.
     */
    public static CommandDto asDto(Command command) {
        return newDto(CommandDto.class).withName(command.getName())
                                       .withCommandLine(command.getCommandLine())
                                       .withType(command.getType())
                                       .withAttributes(command.getAttributes());
    }

    /**
     * Converts {@link ProjectConfig} to {@link ProjectConfigDto}.
     */
    public static ProjectConfigDto asDto(ProjectConfig projectCfg) {
        final ProjectConfigDto projectConfigDto = newDto(ProjectConfigDto.class).withName(projectCfg.getName())
                                                                                .withDescription(projectCfg.getDescription())
                                                                                .withPath(projectCfg.getPath())
                                                                                .withType(projectCfg.getType())
                                                                                .withAttributes(projectCfg.getAttributes())
                                                                                .withMixins(projectCfg.getMixins());
        if (projectCfg.getModules() != null) {
            final List<ProjectConfigDto> modules = projectCfg.getModules()
                                                             .stream()
                                                             .map(DtoConverter::asDto)
                                                             .collect(toList());
            projectConfigDto.withModules(modules);
        }
        final SourceStorage source = projectCfg.getSource();
        if (source != null) {
            projectConfigDto.withSource(newDto(SourceStorageDto.class).withLocation(source.getLocation())
                                                                      .withType(source.getType())
                                                                      .withParameters(source.getParameters()));
        }
        return projectConfigDto;
    }

    //TODO add recipe

    /**
     * Converts {@link Environment} to {@link EnvironmentDto}.
     */
    public static EnvironmentDto asDto(Environment environment) {
        return newDto(EnvironmentDto.class).withName(environment.getName())
                                           .withMachineConfigs(environment.getMachineConfigs()
                                                                                .stream()
                                                                                .map(org.eclipse.che.api.machine.server.DtoConverter::asDto)
                                                                                .collect(toList()));
    }

    /**
     * Converts {@link RuntimeWorkspace} to {@link RuntimeWorkspaceDto}.
     */
    public static RuntimeWorkspaceDto asDto(RuntimeWorkspace workspace) {
        final RuntimeWorkspaceDto runtimeWorkspaceDto = newDto(RuntimeWorkspaceDto.class).withId(workspace.getId())
                                                                                         .withStatus(workspace.getStatus())
                                                                                         .withOwner(workspace.getOwner())
                                                                                         .withTemporary(workspace.isTemporary())
                                                                                         .withConfig(asDto(workspace.getConfig()))
                                                                                         .withActiveEnv(workspace.getActiveEnv())
                                                                                         .withRootFolder(workspace.getRootFolder());
        if (workspace.getMachines() != null) {
            runtimeWorkspaceDto.withMachines(workspace.getMachines()
                                                      .stream()
                                                      .map(org.eclipse.che.api.machine.server.DtoConverter::asDto)
                                                      .collect(toList()));
        }
        if (workspace.getDevMachine() != null) {
            runtimeWorkspaceDto.withDevMachine(org.eclipse.che.api.machine.server.DtoConverter.asDto(workspace.getDevMachine()));
        }
        return runtimeWorkspaceDto;
    }

    public static SnapshotDto asDto(Snapshot snapshot) {
        return newDto(SnapshotDto.class).withId(snapshot.getId())
                                        .withCreationDate(snapshot.getCreationDate())
                                        .withDescription(snapshot.getDescription())
                                        .withDev(snapshot.isDev())
                                        .withOwner(snapshot.getOwner())
                                        .withType(snapshot.getType())
                                        .withWorkspaceId(snapshot.getWorkspaceId())
                                        .withEnvName(snapshot.getEnvName())
                                        .withMachineName(snapshot.getEnvName());
    }

    private DtoConverter() {}
}
