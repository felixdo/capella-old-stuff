/*******************************************************************************
 * Copyright (c) 2016, 2017 THALES GLOBAL SERVICES.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *    Thales Global Services - initial API and implementation
 *******************************************************************************/
package org.polarsys.capella.test.semantic.queries.ju;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * 
 * Activator
 *
 */
public class TestSemanticQueriesPlugin extends Plugin {
  /**
   * The plug-in ID 
   */
  public static final String PLUGIN_ID = "org.polarsys.capella.test.semantic.queries.ju"; //$NON-NLS-1$
  /**
   * The shared instance 
   */
  private static TestSemanticQueriesPlugin plugin;

  /**
   * {@inheritDoc}
   */
  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    super.stop(context);
  }

  /**
   * Returns the shared instance
   * @return the shared instance
   */
  public static TestSemanticQueriesPlugin getDefault() {
    return plugin;
  }
}
