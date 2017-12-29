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
package org.eclipse.che.workspace.infrastructure.dummy.env;

import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.InternalRecipe;

/** @author Sergii Leshchenko */
public class MyInternalEnvironment extends InternalEnvironment {
  public static final String RECIPE_TYPE = "MyRecipe";

  // Any environment specific fields can be declared here

  public MyInternalEnvironment(
      // env specific fields here,
      InternalRecipe recipe, Map<String, InternalMachineConfig> machines, List<Warning> warnings) {
    super(recipe, machines, warnings);
  }
}
