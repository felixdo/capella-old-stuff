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
import org.polarsys.capella.core.data.information.ExchangeItem;

public class FE_FxP_ExchangeItemConsistency extends AbstractComponentValidationRule {

  /**
   * This rule checks that there are no unused EI on FE with respect to its connected FunctionPort
   */
  public static final String UNUSED_FE_EI_ID = "org.polarsys.capella.core.data.cs.validation2.DCON_x2a"; //$NON-NLS-1$

  /**
   * This rule checks that there are no unused EI on FxP with respect to its connected exchanges
   */
  public static final String UNUSED_FXP_EI_ID = "org.polarsys.capella.core.data.cs.validation2.DCON_x2b"; //$NON-NLS-1$


  // all ei on external fe must be on fp
  private <P extends FunctionPort> void validateFEFxP(AbstractFunction func, 
      Function<AbstractFunction, Collection<P>> getFunctionPorts,
      Function<P, Collection<ExchangeItem>> getExchangeItems,
      Function<P, Collection<FunctionalExchange>> getFunctionalExchanges, 
      Component component,
      IValidationContext ctx, Collection<IStatus> errors) {
 
    for (P p : getFunctionPorts.apply(func)) {      
      for (FunctionalExchange fe : getFunctionalExchanges.apply(p)) {
        if (ExchangeExtent.of(fe, component) == ExchangeExtent.INTERFACE) {
          for (ExchangeItem ei : fe.getExchangedItems()) {
            if (!getExchangeItems.apply(p).contains(ei)) {
              Collection<EObject> resultLocus = new ArrayList<EObject>();
              resultLocus.add(component);
              resultLocus.add(ei);
              resultLocus.add(fe);
              resultLocus.add(p);
              errors.add(ConstraintStatus.createStatus(ctx, ei, resultLocus, getMessageTemplate(ctx), ei, fe, p));
            }
          }
        }
      }      
    }
  }

  
  // all ei on fp must be on any fe
  private <P extends FunctionPort> void validateFxPFE(AbstractFunction func, Function<AbstractFunction, Collection<P>> getFunctionPorts,
      Function<P, Collection<ExchangeItem>> getExchangeItems,
      Function<P, Collection<FunctionalExchange>> getFunctionalExchanges, Component component,
      IValidationContext ctx, Collection<IStatus> errors) {
 
    for (P p : getFunctionPorts.apply(func)) {
      Collection<FunctionalExchange> fes = getFunctionalExchanges.apply(p);
      for (ExchangeItem ei : getExchangeItems.apply(p)) {
        boolean missing = true;

        for (FunctionalExchange fe : fes) {
          if (fe.getExchangedItems().contains(ei)) {
            missing = false;
            break;
          }
        }
        if (missing) {
          Collection<EObject> resultLocus = new ArrayList<EObject>();
          resultLocus.add(component);
          resultLocus.add(ei);
          resultLocus.addAll(fes);
          resultLocus.add(p);
          errors.add(ConstraintStatus.createStatus(ctx, ei, resultLocus, getMessageTemplate(ctx), ei, p, fes));
        }
      }
    }
  }

  @Override
  protected void validateComponent(IValidationContext ctx, Component component, Collection<IStatus> errors) {

    PartialValidator validator = null;

    if (UNUSED_FE_EI_ID.equals(ctx.getCurrentConstraintId())) {
      validator = this::validateFEFxP;
    } else if (UNUSED_FXP_EI_ID.equals(ctx.getCurrentConstraintId())) {
      validator = this::validateFxPFE;
    }

    for (AbstractFunction func : component.getAllocatedFunctions()) {
      validator.validate(func,
          ModelHelpers::getFunctionInputPorts,
          FunctionInputPort::getIncomingExchangeItems, 
          (FunctionInputPort fip) -> ModelHelpers.getIncomingInterfaceExchanges(fip, component),
          component, ctx, errors);

      validator.validate(func, 
          ModelHelpers::getFunctionOutputPorts, 
          FunctionOutputPort::getOutgoingExchangeItems, 
          (FunctionOutputPort fop) -> ModelHelpers.getOutgoingInterfaceExchanges(fop, component),
          component, ctx, errors);
    }
  }

  @FunctionalInterface
  private interface PartialValidator {
    public <P extends FunctionPort> void validate(AbstractFunction func, Function<AbstractFunction, Collection<P>> getFunctionPorts,
        Function<P, Collection<ExchangeItem>> getExchangeItems,
        Function<P, Collection<FunctionalExchange>> getFunctionalExchanges, Component component,
        IValidationContext ctx, Collection<IStatus> errors);
  }

}
