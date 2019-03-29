/*******************************************************************************
 * Copyright (c) 2017 THALES GLOBAL SERVICES.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *   
 * Contributors:
 *    Thales - initial API and implementation
 *******************************************************************************/
package org.polarsys.capella.core.data.cs.validation2;

import static org.polarsys.capella.core.data.cs.validation2.helpers.ExchangeItemAllocator.allocate;

import java.util.Collection;

import org.eclipse.emf.ecore.EObject;
import org.polarsys.capella.core.data.information.ExchangeItem;

public class AllocateExchangeItemCommand extends AbstractExchangeItemCommand {

  public AllocateExchangeItemCommand(ExchangeItem ei, Collection<? extends EObject> targets) {
    super(ei, targets,"%allocateExchangeItemCommand_label"); //$NON-NLS-1$
  }

  @Override
  protected void doExecute() {
    allocate(getExchangeItem()).on(getTargets());
  }
}