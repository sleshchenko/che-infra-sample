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
package org.eclipse.che.workspace.infrastructure.dummy.bootstrapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.assistedinject.Assisted;
import java.util.List;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.api.workspace.server.bootstrap.AbstractBootstrapper;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;

/** @author Sergii Leshchenko */
public class MyBootstrapper extends AbstractBootstrapper {
  private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

  private final String machineName;
  private final RuntimeIdentity runtimeIdentity;
  private final List<Installer> installers;
  private final int serverCheckPeriodSeconds;
  private final int installerTimeoutSeconds;
  private final String installerWebsocketEndpoint;
  private final String outputWebsocketEndpoint;

  public MyBootstrapper(
      @Assisted String machineName,
      @Assisted RuntimeIdentity runtimeIdentity,
      @Assisted List<Installer> installers,
      EventService eventService,
      @Named("che.websocket.endpoint") String cheWebsocketEndpoint,
      @Named("che.infra.dummy.output_endpoint") String myOutputEndpoint,
      @Named("che.infra.dummy.bootstrapper.timeout_min") int bootstrappingTimeoutMinutes,
      @Named("che.infra.dummy.bootstrapper.installer_timeout_sec") int installerTimeoutSeconds,
      @Named("che.infra.dummy.bootstrapper.server_check_period_sec") int serverCheckPeriodSeconds) {
    super(
        machineName,
        runtimeIdentity,
        bootstrappingTimeoutMinutes,
        myOutputEndpoint,
        cheWebsocketEndpoint,
        eventService);
    this.machineName = machineName;
    this.runtimeIdentity = runtimeIdentity;
    this.installers = installers;
    this.serverCheckPeriodSeconds = serverCheckPeriodSeconds;
    this.installerTimeoutSeconds = installerTimeoutSeconds;
    this.installerWebsocketEndpoint = cheWebsocketEndpoint;
    this.outputWebsocketEndpoint = myOutputEndpoint;
  }

  @Override
  protected void doBootstrapAsync(String installerWebsocketEndpoint, String outputWebsocketEndpoint)
      throws InfrastructureException {
    // do inject bootstrapper binaries

    // make it executable

    String configJson = GSON.toJson(installers);
    // inject config.json file

    // launch bootstrapper with the corresponding
    // configuration parameters
    //
    // ./bootstrapper -machine-name $machineName
    //                -runtime-id + String.format("%s:%s:%s",
    //                                            runtimeIdentity.getWorkspaceId(),
    //                                            runtimeIdentity.getEnvName(),
    //                                            runtimeIdentity.getOwner()
    //                -push-endpoint $installerWebsocketEndpoint
    //                -push-logs-endpoint $outputWebsocketEndpoint
    //                -server-check-period $serverCheckPeriodSeconds
    //                -installer-timeout $installerTimeoutSeconds
    //                -file $pathToConfigFileHere
  }
}
