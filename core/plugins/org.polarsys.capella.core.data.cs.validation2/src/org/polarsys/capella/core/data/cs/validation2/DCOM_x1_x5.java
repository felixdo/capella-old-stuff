package org.polarsys.capella.core.data.cs.validation2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.validation.IValidationContext;
import org.eclipse.emf.validation.model.ConstraintStatus;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.fa.AbstractFunction;
import org.polarsys.capella.core.data.fa.FunctionInputPort;
import org.polarsys.capella.core.data.fa.FunctionOutputPort;
import org.polarsys.capella.core.data.fa.FunctionPort;
import org.polarsys.capella.core.data.fa.FunctionalExchange;

public class DCOM_x1_x5 extends AbstractComponentValidationRule {

  /**
   * External Functional Exchanges must be allocated to a CP
   */
  public static final String DCOM_X1 = "org.polarsys.capella.core.data.cs.validation2.DCOM_x1"; //$NON-NLS-1$
  
  /**
   * FxP must either be connected to an FE, or allocated to a CP
   */
  private final String DCOM_X5 = "org.polarsys.capella.core.data.cs.validation2.DCOM_x5"; //$NON-NLS-1$
  
  protected void validateComponent(IValidationContext ctx, Component component, Collection<IStatus> errors) {
    for (AbstractFunction f : component.getAllocatedFunctions()) {
      errors.addAll(validatePartial(ctx, component, f, ModelHelpers::getFunctionInputPorts, FunctionInputPort::getIncomingFunctionalExchanges));
      errors.addAll(validatePartial(ctx, component, f, ModelHelpers::getFunctionOutputPorts, FunctionOutputPort::getOutgoingFunctionalExchanges));
    }
  }

  private <FxP extends FunctionPort> Collection<IStatus> validatePartial(IValidationContext ctx, Component c, AbstractFunction f, Function<AbstractFunction, Collection<FxP>> getFxP, Function<FxP, Collection<FunctionalExchange>> getFxPExchanges) {
    Collection<IStatus> errors = new ArrayList<IStatus>();
    Component component = (Component) ctx.getTarget();

    for (FxP fxp : getFxP.apply(f)) {
      if (DCOM_X1.equals(ctx.getCurrentConstraintId())) {      
        for (FunctionalExchange fe : getFxPExchanges.apply(fxp)) {
          if (ExchangeExtent.of(fe, component) == ExchangeExtent.INTERFACE) {
            if (fxp.getAllocatorComponentPorts().isEmpty()) {
              Collection<EObject> resultLocus = new ArrayList<EObject>();
              resultLocus.add(c);
              resultLocus.add(f);
              resultLocus.add(fxp);
              resultLocus.add(fe);
              errors.add(ConstraintStatus.createStatus(ctx, fxp, resultLocus, getMessageTemplate(ctx), fxp));
            }
          }
        }
      } else if (DCOM_X5.equals(ctx.getCurrentConstraintId())) {
        if (fxp.getAllocatorComponentPorts().isEmpty() && getFxPExchanges.apply(fxp).isEmpty()) {
          Collection<EObject> resultLocus = new ArrayList<EObject>();
          resultLocus.add(c);
          resultLocus.add(f);
          resultLocus.add(fxp);
          errors.add(ConstraintStatus.createStatus(ctx, fxp, null, getMessageTemplate(ctx), fxp));
        }
      } else {
        // for sure will the rule IDs change so remember to rename the constants..
        throw new RuntimeException("Rule IDs have changed, rename internal constant.."); //$NON-NLS-1$
      }
    }
    return errors;
  }

}
