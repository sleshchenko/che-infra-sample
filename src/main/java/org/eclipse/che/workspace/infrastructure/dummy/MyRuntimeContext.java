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

import static java.util.Collections.emptyList;

import com.google.inject.assistedinject.Assisted;
import java.net.URI;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.workspace.infrastructure.dummy.env.MyInternalEnvironment;

/** @author Sergii Leshchenko */
public class MyRuntimeContext extends RuntimeContext<MyInternalEnvironment> {

  private final MyRuntimeFactory myRuntimeFactory;
  private final String outputEndpoint;

  @Inject
  public MyRuntimeContext(
      @Assisted MyInternalEnvironment internalEnv,
      @Assisted RuntimeIdentity identity,
      @Assisted RuntimeInfrastructure infra,
      MyRuntimeFactory myRuntimeFactory,
      @Named("che.infra.dummy.output_endpoint") String outputEndpoint)
      throws ValidationException, InfrastructureException {
    super(internalEnv, identity, infra);
    this.myRuntimeFactory = myRuntimeFactory;
    this.outputEndpoint = outputEndpoint;
  }

  @Override
  public URI getOutputChannel() throws InfrastructureException, UnsupportedOperationException {
    try {
      return URI.create(outputEndpoint);
    } catch (IllegalArgumentException ex) {
      throw new InternalInfrastructureException(
          "Failed to get the output channel.  " + ex.getMessage());
    }
  }

  @Override
  public InternalRuntime getRuntime() throws InfrastructureException {
    return myRuntimeFactory.create(this, emptyList());
  }
}
