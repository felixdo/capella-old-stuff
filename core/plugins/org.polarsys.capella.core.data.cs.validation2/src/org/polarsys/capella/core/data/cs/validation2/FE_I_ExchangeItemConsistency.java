package org.polarsys.capella.core.data.cs.validation2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.validation.IValidationContext;
import org.eclipse.emf.validation.model.ConstraintStatus;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.cs.Interface;
import org.polarsys.capella.core.data.fa.AbstractFunction;
import org.polarsys.capella.core.data.fa.ComponentPort;
import org.polarsys.capella.core.data.fa.FunctionPort;
import org.polarsys.capella.core.data.fa.FunctionalExchange;
import org.polarsys.capella.core.data.information.ExchangeItem;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/** 
 * For every EI on FE, the EI must be on at least one 'related' I. The relation comes in two variants:
 * 1) By checking all I provided/required by source/target component, regardless of FxP-CP allocations (DCON_4xb)
 * 2) By checking all I provided/required by source/target component and respecting FxP-CP allocations (DCON_3xb)
 */
public class FE_I_ExchangeItemConsistency extends AbstractComponentValidationRule {

  public final static String NO_UNUSED_EI_ON_FE_ID = "org.polarsys.capella.core.data.cs.validation2.DCON_x4b"; //$NON-NLS-1$
  public final static String NO_UNUSED_EI_ON_FE_WITH_ALLOCATIONS_ID = "org.polarsys.capella.core.data.cs.validation2.DCON_x3b"; //$NON-NLS-1$

  protected void validateComponent(IValidationContext ctx, Component component, Collection<IStatus> errors) {
    validateIncomingExchanges(ctx, component, errors);
    validateOutgoingExchanges(ctx, component, errors);
  }

  private void validateOutgoingExchanges(IValidationContext ctx, Component component, Collection<IStatus> errors) {
    validatePartial(ctx, component, errors, 
        getString("%required"), //$NON-NLS-1$
        getString("%provided"), //$NON-NLS-1$
        ComponentPort::getRequiredInterfaces,
        ComponentPort::getProvidedInterfaces,
        (fxp) -> ModelHelpers.getOutgoingInterfaceExchanges(fxp, component),
        ModelHelpers::getFunctionOutputPorts,
        ModelHelpers::getFunctionInputPorts,
        FunctionalExchange::getTargetFunctionInputPort
        );
  }

  private void validateIncomingExchanges(IValidationContext ctx, Component component, Collection<IStatus> errors) {
    validatePartial(ctx, component, errors, 
        getString("%provided"), //$NON-NLS-1$
        getString("%required"), //$NON-NLS-1$
        ComponentPort::getProvidedInterfaces,
        ComponentPort::getRequiredInterfaces,
        (fxp) -> ModelHelpers.getIncomingInterfaceExchanges(fxp, component),
        ModelHelpers::getFunctionInputPorts,
        ModelHelpers::getFunctionOutputPorts,
        FunctionalExchange::getSourceFunctionOutputPort
        );
  }

  private Function<FunctionPort, Collection<ComponentPort>> getLeftAllocatingComponentPorts(IValidationContext ctx){
    if (NO_UNUSED_EI_ON_FE_WITH_ALLOCATIONS_ID.equals(ctx.getCurrentConstraintId())) {
      return FunctionPort::getAllocatorComponentPorts;
    } else if (NO_UNUSED_EI_ON_FE_ID.equals(ctx.getCurrentConstraintId())) {
      return ModelHelpers::getAllocatorComponentPortsWithFallback;
    } else {
      throw new IllegalArgumentException("Unrecognized validation rule id: " + ctx.getCurrentConstraintId()); //$NON-NLS-1$
    }
  }
  
  //always fall back to all component ports if no allocation exists
  private Function<FunctionPort, Collection<ComponentPort>> getRightAllocatingComponentPorts(IValidationContext ctx){
    return ModelHelpers::getAllocatorComponentPortsWithFallback;
  }

  private <FxP extends FunctionPort, FyP extends FunctionPort> void validatePartial(IValidationContext ctx, Component leftComponent, Collection<IStatus> errors, 
      String leftRole, String rightRole,
      Function<ComponentPort, Collection<Interface>> getFxPInterfaces,
      Function<ComponentPort, Collection<Interface>> getFyPInterfaces,
      Function<FxP, Collection<FunctionalExchange>> getFxPFe,
      Function<AbstractFunction, Collection<FxP>> getFunctionFxP,
      Function<AbstractFunction, Collection<FyP>> getFunctionFyP,
      Function<FunctionalExchange, FyP> getFEFyp
      ) {

    for (AbstractFunction func : leftComponent.getAllocatedFunctions()) {  

      for (FxP fxp : getFunctionFxP.apply(func)) {

        // there are cases where the interface port component and the function component
        // are not the same, e.g. when the function port is allocated to a cp on a parent
        // component.
        Multimap<Interface, Component> leftCandidateMap = LinkedHashMultimap.create();
        
        for (ComponentPort fxpCP : getLeftAllocatingComponentPorts(ctx).apply(fxp)) {
          
          // null key is used to remember which components were searched for candidate interfaces
          leftCandidateMap.put(null, (Component) fxpCP.eContainer());

          for (Interface i : getFxPInterfaces.apply(fxpCP)) {
            leftCandidateMap.put(i, (Component) fxpCP.eContainer());            
          }
        }

        // relation fxp-cp is empty...
        if (leftCandidateMap.get(null).isEmpty()) {
          leftCandidateMap.put(null, leftComponent);
        }

        for (FunctionalExchange fe : getFxPFe.apply(fxp)) {

          FyP fyp = getFEFyp.apply(fe);

          Multimap<Interface, Component> rightCandidateMap = LinkedHashMultimap.create();

          for (ComponentPort fypCP : getRightAllocatingComponentPorts(ctx).apply(fyp)) {
            rightCandidateMap.put(null, (Component) fypCP.eContainer());
            for (Interface i : getFyPInterfaces.apply(fypCP)) {
              rightCandidateMap.put(i, (Component) fypCP.eContainer());
            }
          }

          // relation fyp-cp is empty
          if (rightCandidateMap.get(null).isEmpty()) {
            rightCandidateMap.put(null, ModelHelpers.getDefaultAllocatorComponent(fyp));
          }

          Set<Interface> candidateInterfaces = new HashSet<>(leftCandidateMap.keySet());         
          candidateInterfaces.retainAll(rightCandidateMap.keySet());
          candidateInterfaces.remove(null);

          for (ExchangeItem ei : fe.getExchangedItems()) {
            if (isMissing(ei, candidateInterfaces)) {              
              Collection<EObject> resultLocus = new ArrayList<EObject>();
              resultLocus.addAll(candidateInterfaces);
              resultLocus.add(ei);
              resultLocus.add(fe);
              errors.add(ConstraintStatus.createStatus(ctx, ei, resultLocus, getMessageTemplate(ctx), leftRole, leftCandidateMap.get(null), rightRole, rightCandidateMap.get(null), ei, fe));
            }
          }
        }
      }
    }
  }

  private boolean isMissing(ExchangeItem ei, Collection<Interface> candidates) {
    return isMissing(ei, candidates, Interface::getExchangeItems);
  }

  private <T extends EObject> boolean isMissing(ExchangeItem needle, Collection<T> haystack, Function<T, Collection<ExchangeItem>> transfo) {
    for (T t : haystack) {
      if (transfo.apply(t).contains(needle)) {
        return false;
      }
    }
    return true;
  }

}


