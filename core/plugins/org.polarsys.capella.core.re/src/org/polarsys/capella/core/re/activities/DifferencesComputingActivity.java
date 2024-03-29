/*******************************************************************************
 * Copyright (c) 2006, 2016 THALES GLOBAL SERVICES.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *    Thales - initial API and implementation
 *******************************************************************************/
package org.polarsys.capella.core.re.activities;

import org.eclipse.emf.diffmerge.api.IMergePolicy;
import org.polarsys.capella.core.re.policies.merge.CapellaMergePolicy;
import org.polarsys.kitalpha.transposer.rules.handler.rules.api.IContext;

/**
 *
 */
public class DifferencesComputingActivity
    extends org.polarsys.capella.common.re.activities.DifferencesComputingActivity {

  public static final String ID = DifferencesComputingActivity.class.getCanonicalName();

  @Override
  protected IMergePolicy createMergePolicy(IContext context) {
    return new CapellaMergePolicy(context);
  }

}
