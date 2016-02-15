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

import org.eclipse.che.api.core.model.machine.Limits;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineMetadata;
import org.eclipse.che.api.core.model.machine.MachineRuntimeInfo;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.api.core.model.machine.MachineProcess;
import org.eclipse.che.api.core.model.machine.Snapshot;
import org.eclipse.che.api.machine.shared.dto.LimitsDto;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.MachineMetadataDto;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.machine.shared.dto.MachineRuntimeInfoDto;
import org.eclipse.che.api.machine.shared.dto.MachineSourceDto;
import org.eclipse.che.api.machine.shared.dto.ServerDto;
import org.eclipse.che.api.machine.shared.dto.SnapshotDto;

import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

// TODO! use global registry for DTO converters

/**
 * Helps to convert to/from DTOs related to workspace.
 *
 * @author Eugene Voevodin
 */
public final class DtoConverter {
    /**
     * Converts {@link MachineConfig} to {@link MachineConfigDto}.
     */
    public static MachineConfigDto asDto(MachineConfig config) {
        return newDto(MachineConfigDto.class).withName(config.getName())
                                             .withType(config.getType())
                                             .withDev(config.isDev())
                                             .withLimits(asDto(config.getLimits()))
                                             .withSource(asDto(config.getSource()));
    }

    /**
     * Converts {@link MachineSource} to {@link MachineSourceDto}.
     */
    public static MachineSourceDto asDto(MachineSource source) {
        return newDto(MachineSourceDto.class).withType(source.getType()).withLocation(source.getLocation());
    }

    /**
     * Converts {@link Limits} to {@link LimitsDto}.
     */
    public static LimitsDto asDto(Limits limits) {
        return newDto(LimitsDto.class).withRam(limits.getRam());
    }

    /**
     * Converts {@link Machine} to {@link MachineDto}.
     */
    public static MachineDto asDto(Machine machine) {
        final MachineDto machineDto = newDto(MachineDto.class).withConfig(asDto(machine.getConfig()))
                                                              .withId(machine.getId())
                                                              .withStatus(machine.getStatus())
                                                              .withOwner(machine.getOwner())
                                                              .withWorkspaceId(machine.getWorkspaceId());
        if (machine.getRuntime() != null) {
            machineDto.withRuntime(asDto(machine.getRuntime()));
        }
        return machineDto;
    }

    private static MachineRuntimeInfoDto asDto(MachineRuntimeInfo runtime) {
        return newDto(MachineRuntimeInfoDto.class).withMetadata(asDto(runtime.getMetadata()));
    }

    /**
     * Converts {@link Limits} to {@link LimitsDto}.
     */
    public static MachineMetadataDto asDto(MachineMetadata metadata) {
        final Map<String, ServerDto> servers = metadata.getServers()
                                                       .entrySet()
                                                       .stream()
                                                       .collect(toMap(Map.Entry::getKey, entry -> asDto(entry.getValue())));

        return newDto(MachineMetadataDto.class).withEnvVariables(metadata.getEnvVariables())
                                               .withProperties(metadata.getProperties())
                                               .withServers(servers);
    }

    /**
     * Converts {@link Server} to {@link ServerDto}.
     */
    public static ServerDto asDto(Server server) {
        return newDto(ServerDto.class).withAddress(server.getAddress())
                                      .withRef(server.getRef())
                                      .withUrl(server.getUrl());
    }

    /**
     * Converts {@link Snapshot} to {@link SnapshotDto}.
     */
    public static SnapshotDto asDto(Snapshot snapshot) {
        return newDto(SnapshotDto.class).withType(snapshot.getType())
                                        .withDescription(snapshot.getDescription())
                                        .withCreationDate(snapshot.getCreationDate())
                                        .withDev(snapshot.isDev())
                                        .withId(snapshot.getId())
                                        .withOwner(snapshot.getOwner())
                                        .withWorkspaceId(snapshot.getWorkspaceId())
                                        .withLinks(null);
    }

    /**
     * Converts {@link MachineProcess} to {@link MachineProcessDto}.
     */
    public static MachineProcessDto asDto(MachineProcess machineProcess) {
        return newDto(MachineProcessDto.class).withPid(machineProcess.getPid())
                                              .withCommandLine(machineProcess.getCommandLine())
                                              .withAlive(machineProcess.isAlive())
                                              .withName(machineProcess.getName())
                                              .withType(machineProcess.getType())
                                              .withOutputChannel(machineProcess.getOutputChannel())
                                              .withLinks(null);
    }

    private DtoConverter() {
    }
}
