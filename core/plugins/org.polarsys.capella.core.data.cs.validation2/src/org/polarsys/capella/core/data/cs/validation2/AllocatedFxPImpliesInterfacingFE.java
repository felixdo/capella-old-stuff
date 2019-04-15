package org.polarsys.capella.core.data.cs.validation2;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.validation.IValidationContext;
import org.eclipse.emf.validation.model.ConstraintStatus;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.fa.ComponentPort;
import org.polarsys.capella.core.data.fa.FunctionInputPort;
import org.polarsys.capella.core.data.fa.FunctionOutputPort;
import org.polarsys.capella.core.data.fa.FunctionPort;
import org.polarsys.capella.core.validation.rule.AbstractValidationRule;

/* 
 * A FxP that is allocated to a CP must have an interfacing exchange
 */
public class AllocatedFxPImpliesInterfacingFE extends AbstractValidationRule {

  static final ValidationUtil util = new ValidationUtil(Activator.getContext().getBundle());
  
  @Override
  public IStatus validate(IValidationContext ctx) {
    FunctionPort fp = (FunctionPort) ctx.getTarget();
    Set<Component> allocatorComponents = new HashSet<>();
    for (ComponentPort cp : fp.getAllocatorComponentPorts()){
      allocatorComponents.add((Component) cp.eContainer());
    }

    for (Component component : allocatorComponents) {
      if (fp instanceof FunctionInputPort) {      
        if (ModelHelpers.getIncomingInterfaceExchanges((FunctionInputPort) fp, component).isEmpty()) {
          return ConstraintStatus.createStatus(ctx, fp, null, util.getMessageTemplate(ctx), fp);
        }
      } else if (fp instanceof FunctionOutputPort) {
        if (ModelHelpers.getOutgoingInterfaceExchanges((FunctionOutputPort) fp, component).isEmpty()) {
          return ConstraintStatus.createStatus(ctx, fp, null, util.getMessageTemplate(ctx), fp);
        }
      }
    }
    return ctx.createSuccessStatus();
  }

}
