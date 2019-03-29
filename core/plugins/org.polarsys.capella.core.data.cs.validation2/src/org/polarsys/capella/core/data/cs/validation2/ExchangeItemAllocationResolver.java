package org.polarsys.capella.core.data.cs.validation2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.validation.model.IConstraintStatus;
import org.polarsys.capella.core.data.cs.Interface;
import org.polarsys.capella.core.data.fa.ComponentPort;
import org.polarsys.capella.core.data.fa.FunctionInputPort;
import org.polarsys.capella.core.data.fa.FunctionOutputPort;
import org.polarsys.capella.core.data.fa.FunctionPort;
import org.polarsys.capella.core.data.fa.FunctionalExchange;
import org.polarsys.capella.core.data.information.ExchangeItem;

public class ExchangeItemAllocationResolver implements CommandResolutionGenerator {

  public ExchangeItemAllocationResolver(boolean confirmAllocationDeletions) {
    this.confirmAllocationDeletions = confirmAllocationDeletions;
  }
  
  private final boolean confirmAllocationDeletions;
  
  protected Collection<? extends Command> getResolutionCommands(ExchangeItem ei, Collection<? extends EObject> resultLocus) {

    Collection<Command> commands = new ArrayList<Command>();
    
    Collection<EObject> deallocateCollection = new ArrayList<EObject>();
    Collection<EObject> allocateCollection = new ArrayList<EObject>();
    
    for (EObject e : resultLocus) {
      if (e instanceof Interface) {
        if (((Interface) e).getExchangeItems().contains(ei)) {
          deallocateCollection.add(e);
        } else {
          allocateCollection.add(e);
        }
      } else if (e instanceof FunctionOutputPort) {
        if (((FunctionOutputPort) e).getOutgoingExchangeItems().contains(ei)) {
          deallocateCollection.add(e);
        } else {
          allocateCollection.add(e);
        }
      } else if (e instanceof FunctionInputPort) {
        if (((FunctionInputPort)e).getIncomingExchangeItems().contains(ei)){
          deallocateCollection.add(e);
        } else {
          allocateCollection.add(e);
        } 
      } else if (e instanceof FunctionalExchange) {
        if (((FunctionalExchange)e).getExchangedItems().contains(ei)) {
          deallocateCollection.add(e);
        } else {
          allocateCollection.add(e);
        }
      }
    }

    for (EObject allocateTarget : allocateCollection) {
      commands.add(new AllocateExchangeItemCommand(ei, Collections.singleton(allocateTarget)));
    }

    for (EObject deallocateTarget : deallocateCollection) {
      commands.add(new DeallocateExchangeItemCommand(ei, Collections.singleton(deallocateTarget), confirmAllocationDeletions));
    }

    return commands;

  }

  @Override
  public Collection<? extends Command> generateResolutionCommands(IConstraintStatus cs) {

    if (cs.getTarget() instanceof ExchangeItem && Activator.getContext().getBundle().getSymbolicName().equals(cs.getConstraint().getDescriptor().getPluginId())) {

      Collection<EObject> resultLocus = cs.getResultLocus();

      if (DCON_x1a.RULE_ID.equals(cs.getConstraint().getDescriptor().getId())) {

        // To reduce #elements in the goto menu, the fxp are not part of the result locus. 
        // so now, re-add them for the quickfix

        resultLocus = new ArrayList<>();

        for (EObject e : cs.getResultLocus()) {
          resultLocus.add(e);
          if (e instanceof ComponentPort) {
            Collection<? extends FunctionPort> fxp = Collections.emptyList();
            switch (cs.getCode()) {
            case DCON_x1a.FIPS: fxp = ModelHelpers.getAllocatedFunctionInputPorts((ComponentPort) e); break;
            case DCON_x1a.FOPS: fxp = ModelHelpers.getAllocatedFunctionOutputPorts((ComponentPort) e); break;
            default: break; 
            }
            resultLocus.addAll(fxp);
          }
        }

      }
      return getResolutionCommands((ExchangeItem) cs.getTarget(), resultLocus);
    }

    return Collections.emptyList();
  }

}
