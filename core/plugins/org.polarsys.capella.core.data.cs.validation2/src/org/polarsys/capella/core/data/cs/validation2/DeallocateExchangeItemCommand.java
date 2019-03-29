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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.polarsys.capella.common.ef.ExecutionManager;
import org.polarsys.capella.common.ef.ExecutionManagerRegistry;
import org.polarsys.capella.common.helpers.EObjectExt;
import org.polarsys.capella.core.data.cs.CsPackage;
import org.polarsys.capella.core.data.cs.ExchangeItemAllocation;
import org.polarsys.capella.core.data.cs.Interface;
import org.polarsys.capella.core.data.fa.ComponentExchange;
import org.polarsys.capella.core.data.fa.FunctionInputPort;
import org.polarsys.capella.core.data.fa.FunctionOutputPort;
import org.polarsys.capella.core.data.fa.FunctionalExchange;
import org.polarsys.capella.core.data.information.ExchangeItem;
import org.polarsys.capella.core.platform.sirius.ui.commands.CapellaDeleteCommand;

/**
 * Deallocates a single {@link ExchangeItem} from a collection of target objects. Supported target types are
 * <ul>
 * <li>{@link FunctionalExchange}
 * <li>{@link ComponentExchange}
 * <li>{@link Interface}
 * <li>{@link FunctionInputPort}
 * <li>{@link FunctionOutputPort}
 * </ul>
 * For FunctionalExchange/ComponentExchange, the command removes the exchange item from
 * {@link FunctionalExchange#getExchangedItems() exchangedItems}/ {@link ComponentExchange#getConvoyedInformations()
 * convoyedInformations}. For Interfaces, matching {@link ExchangeItemAllocation} objects are deleted, so this will open
 * the capella delete dialog for confirmation.
 * 
 * TODO merge with class in capella core code
 */
public class DeallocateExchangeItemCommand extends AbstractExchangeItemCommand {

  protected boolean cancelled = false;
  final boolean confirmDeletion;

  public DeallocateExchangeItemCommand(ExchangeItem exchangeItem, Collection<? extends EObject> targets, boolean confirmDeletion) {
    super(exchangeItem, targets, "%deallocateExchangeItemCommand_label"); //$NON-NLS-1$
    this.confirmDeletion = confirmDeletion;
  }

  @Override
  protected void doExecute() {

    Collection<ExchangeItemAllocation> allocationsToDelete = new ArrayList<ExchangeItemAllocation>();

    for (EObject e : getTargets()) {
      if (e instanceof FunctionalExchange) {
        ((FunctionalExchange) e).getExchangedItems().remove(getExchangeItem());
      } else if (e instanceof ComponentExchange) {
        ((ComponentExchange) e).getConvoyedInformations().remove(getExchangeItem());
      } else if (e instanceof Interface) {
        for (EObject allocation : EObjectExt.getReferencers(getExchangeItem(),
            CsPackage.Literals.EXCHANGE_ITEM_ALLOCATION__ALLOCATED_ITEM)) {
          if (((ExchangeItemAllocation) allocation).getAllocatingInterface() == e) {
            allocationsToDelete.add((ExchangeItemAllocation) allocation);
          }
        }
      } else if (e instanceof FunctionInputPort) {
        ((FunctionInputPort) e).getIncomingExchangeItems().remove(getExchangeItem());
      } else if (e instanceof FunctionOutputPort) {
        ((FunctionOutputPort) e).getOutgoingExchangeItems().remove(getExchangeItem());
      }
    }
    deleteAllocations(allocationsToDelete);
  }

  protected void deleteAllocations(Collection<ExchangeItemAllocation> allocationsToDelete) {
    if (allocationsToDelete.isEmpty()) {
      return;
    }
    ExecutionManager manager = ExecutionManagerRegistry.getInstance().getExecutionManager(TransactionUtil.getEditingDomain(getExchangeItem()));
    CapellaDeleteCommand command = new CapellaDeleteCommand(manager, allocationsToDelete, false, confirmDeletion, true);
    if (!command.canExecute()) {
      cancel();
    }
    command.execute();
    if (confirmDeletion) {
      // TODO no better way to check if user canceled deletion?
      for (ExchangeItemAllocation eia : allocationsToDelete) {
        if (eia.eResource() != null) {
          cancel();
        }
      }
    }
  }

  private void cancel() {
    cancelled = true;
    throw new OperationCanceledException();
  }
  
  @Override
  public Collection<?> getResult() {
    if (cancelled) {
      return Collections.singleton(Status.CANCEL_STATUS);
    }
    return super.getResult();
  }
}