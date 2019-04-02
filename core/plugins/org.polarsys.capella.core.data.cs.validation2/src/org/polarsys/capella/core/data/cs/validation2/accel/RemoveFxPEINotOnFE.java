package org.polarsys.capella.core.data.cs.validation2.accel;

import static org.polarsys.capella.core.data.cs.validation2.ModelHelpers.createCommandCollector;
import static org.polarsys.capella.core.data.cs.validation2.ModelHelpers.asCommand;
import static org.polarsys.capella.core.data.cs.validation2.ModelHelpers.getExchangeItems;
import static org.polarsys.capella.core.data.cs.validation2.accel.HideAffectedObjectsCommandWrapper.hide;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.polarsys.capella.core.data.cs.validation2.ModelHelpers;
import org.polarsys.capella.core.data.cs.validation2.accel.common.DomainHandler;
import org.polarsys.capella.core.data.fa.FunctionPort;
import org.polarsys.capella.core.data.information.ExchangeItem;

// For a function port, remove all EI which are on none of its connected FE
public class RemoveFxPEINotOnFE extends DomainHandler<FunctionPort> {

  public RemoveFxPEINotOnFE() {
    super(FunctionPort.class);
  }

  @Override
  protected Command createCommand(ExecutionEvent event, TransactionalEditingDomain domain, Stream<FunctionPort> list) {
    return list.collect(createCommandCollector(
        (FunctionPort fp) -> 
          hide(asCommand(domain, () -> getExchangeItems(fp).retainAll(getAllConnectedFEEI(fp))))
        ));
  }

  private Collection<ExchangeItem> getAllConnectedFEEI(FunctionPort fp){
    return ModelHelpers.getFunctionalExchanges(fp).stream().flatMap(
        fe -> fe.getExchangedItems().stream()).collect(Collectors.toSet());
  }
}
