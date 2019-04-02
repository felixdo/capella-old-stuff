package org.polarsys.capella.core.data.cs.validation2.accel;

import static org.polarsys.capella.core.data.cs.validation2.ModelHelpers.createCommandCollector;
import static org.polarsys.capella.core.data.cs.validation2.ModelHelpers.getExchangeItems;
import static org.polarsys.capella.core.data.cs.validation2.ModelHelpers.getFunctionalExchanges;
import static org.polarsys.capella.core.data.cs.validation2.accel.HideAffectedObjectsCommandWrapper.hide;

import java.util.stream.Stream;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.polarsys.capella.core.data.cs.validation2.accel.common.DomainHandler;
import org.polarsys.capella.core.data.fa.FaPackage;
import org.polarsys.capella.core.data.fa.FunctionPort;
import org.polarsys.capella.core.data.fa.FunctionalExchange;

// For a function port, put all its EI onto all its connected FE
public class AllocateAllExchangeItemsHandler extends DomainHandler<FunctionPort> {
  
  private final EReference ref = FaPackage.Literals.FUNCTIONAL_EXCHANGE__EXCHANGED_ITEMS;
  
  public AllocateAllExchangeItemsHandler() {
    super(FunctionPort.class);
  }

  @Override
  protected Command createCommand(ExecutionEvent event, TransactionalEditingDomain domain, Stream<FunctionPort> list) {
    return list.collect(createCommandCollector(
        (FunctionPort fp) -> getFunctionalExchanges(fp).stream()
          .collect(createCommandCollector(
              (FunctionalExchange fe) -> hide(AddCommand.create(domain, fe, ref, getExchangeItems(fp)))))
    ));
  }

}
