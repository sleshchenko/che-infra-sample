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
package org.eclipse.che.workspace.infrastructure.dummy;

import static java.util.Collections.*;

import com.google.inject.assistedinject.Assisted;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.api.workspace.server.hc.ServersChecker;
import org.eclipse.che.api.workspace.server.hc.ServersCheckerFactory;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.api.workspace.shared.dto.event.ServerStatusEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.workspace.infrastructure.dummy.bootstrapper.MyBootstrapper;
import org.eclipse.che.workspace.infrastructure.dummy.bootstrapper.MyBootstrapperFactory;
import org.eclipse.che.workspace.infrastructure.dummy.env.MyInternalEnvironment;

/** @author Sergii Leshchenko */
public class MyInternalRuntime extends InternalRuntime<MyRuntimeContext> {

  private final MyBootstrapperFactory myBootstrapperFactory;
  private final ServersCheckerFactory serverCheckerFactory;
  private final EventService eventService;

  @Inject
  public MyInternalRuntime(
      @Assisted MyRuntimeContext context,
      @Assisted List<Warning> warnings,
      URLRewriter urlRewriter,
      MyBootstrapperFactory myBootstrapperFactory,
      ServersCheckerFactory serverCheckerFactory,
      EventService eventService) {
    super(context, urlRewriter, warnings, false);
    this.myBootstrapperFactory = myBootstrapperFactory;
    this.serverCheckerFactory = serverCheckerFactory;
    this.eventService = eventService;
  }

  @Override
  protected Map<String, ? extends Machine> getInternalMachines() {
    // Internal runtime should return list of machine with resolved servers here
    return emptyMap();
  }

  @Override
  protected void internalStart(Map<String, String> startOptions) throws InfrastructureException {
    MyInternalEnvironment envToStart = getContext().getEnvironment();
    try {
      for (Entry<String, InternalMachineConfig> machineConfigEntry :
          envToStart.getMachines().entrySet()) {
        doStartMachine(machineConfigEntry);
      }
    } catch (Exception e) {
      // clean up resources here.
      try {
        throw e;
      } catch (InfrastructureException rethrow) {
        throw rethrow;
      } catch (Exception wrap) {
        throw new InternalInfrastructureException(e.getMessage(), wrap);
      }
    }
  }

  private void doStartMachine(Entry<String, InternalMachineConfig> machineConfigEntry)
      throws InfrastructureException, InterruptedException {
    String machineName = machineConfigEntry.getKey();
    InternalMachineConfig machineConfig = machineConfigEntry.getValue();

    sendMachineStartingEvent(machineName);

    try {
      doStartMachine(machineName, machineConfig);

      sendMachineRunningEvent(machineName);

      doBootstrap(machineName, machineConfig);

      doWaitServersRunning(machineName, machineConfig);
    } catch (Exception e) {
      sendMachineFailedEvent(machineName, e.getMessage());
      throw e;
    }
  }

  private void doStartMachine(String machineName, InternalMachineConfig machineConfig) {
    MyInternalEnvironment internalEnv = getContext().getEnvironment();
    // perform start of the machine using the specified machine config
    // and infrastructure specific objects from the internal environment
  }

  private void doWaitServersRunning(String machineName, InternalMachineConfig machineConfig)
      throws InfrastructureException, InterruptedException {
    ServersChecker readinessChecker =
        serverCheckerFactory.create(
            getContext().getIdentity(),
            machineName,
            // resolved machine servers should be here instead of empty map
            emptyMap());
    readinessChecker.startAsync(
        (serverRef) -> {
          // update server state to RUNNING
          sendRunningServerEvent(machineName, serverRef);
        });
    readinessChecker.await();

    // or custom infrastructure specific logic can be performed here
  }

  private void doBootstrap(String machineName, InternalMachineConfig machineConfig)
      throws InfrastructureException, InterruptedException {
    // boostrapper can be used here
    MyBootstrapper myBootstrapper =
        myBootstrapperFactory.create(
            machineName, getContext().getIdentity(), machineConfig.getInstallers());

    myBootstrapper.bootstrap();

    // or custom infrastructure specific logic performed
  }

  @Override
  protected void internalStop(Map<String, String> stopOptions) throws InfrastructureException {}

  @Override
  public Map<String, String> getProperties() {
    return new HashMap<>();
  }

  private void sendRunningServerEvent(String machineName, String serverRef) {
    eventService.publish(
        DtoFactory.newDto(ServerStatusEvent.class)
            .withIdentity(DtoConverter.asDto(getContext().getIdentity()))
            .withMachineName(machineName)
            .withServerName(serverRef)
            .withStatus(ServerStatus.RUNNING));
  }

  private void sendMachineStartingEvent(String machineName) {
    eventService.publish(
        DtoFactory.newDto(MachineStatusEvent.class)
            .withIdentity(DtoConverter.asDto(getContext().getIdentity()))
            .withEventType(MachineStatus.STARTING)
            .withMachineName(machineName));
  }

  private void sendMachineRunningEvent(String machineName) {
    eventService.publish(
        DtoFactory.newDto(MachineStatusEvent.class)
            .withIdentity(DtoConverter.asDto(getContext().getIdentity()))
            .withEventType(MachineStatus.RUNNING)
            .withMachineName(machineName));
  }

  private void sendMachineFailedEvent(String machineName, String message) {
    eventService.publish(
        DtoFactory.newDto(MachineStatusEvent.class)
            .withIdentity(DtoConverter.asDto(getContext().getIdentity()))
            .withEventType(MachineStatus.FAILED)
            .withMachineName(machineName)
            .withError(message));
  }
}
