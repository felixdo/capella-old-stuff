package org.polarsys.capella.core.data.cs.validation2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.validation.IValidationContext;
import org.eclipse.emf.validation.model.ConstraintStatus;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.cs.Interface;
import org.polarsys.capella.core.data.fa.ComponentPort;
import org.polarsys.capella.core.data.fa.FunctionInputPort;
import org.polarsys.capella.core.data.fa.FunctionOutputPort;
import org.polarsys.capella.core.data.fa.FunctionPort;
import org.polarsys.capella.core.data.fa.FunctionalExchange;
import org.polarsys.capella.core.data.information.ExchangeItem;

/** 
 * For every EI on Interface, the EI must be on at least one 'related' FE. The relation comes in two variants:
 * 1) By checking all FE of the component, i.e. regardless of FxP-CP allocations (DCON_4xa)
 * 2) By checking FE related via port allocations FxP-CP (DCON_3xa)
 */
public class I_FE_ExchangeItemConsistency extends AbstractComponentValidationRule {

  public final static String NO_UNUSED_EI_ON_I_ID = "org.polarsys.capella.core.data.cs.validation2.DCON_x4a"; //$NON-NLS-1$
  public final static String NO_UNUSED_EI_ON_I_WITH_ALLOCATIONS = "org.polarsys.capella.core.data.cs.validation2.DCON_x3a"; //$NON-NLS-1$

  protected void validateComponent(IValidationContext ctx, Component component, Collection<IStatus> errors) {
    validateProvidedInterfaces(ctx, component, errors);
    validateRequiredInterfaces(ctx, component, errors);
  }

  private void validateRequiredInterfaces(IValidationContext ctx, Component component, Collection<IStatus> errors) {
    validatePartial(ctx, component, errors,
        getString("%required"), //$NON-NLS-1$
        getString("%provided"), //$NON-NLS-1$
        ComponentPort::getRequiredInterfaces,
        Interface::getRequiringComponentPorts,
        Interface::getProvidingComponentPorts,
        getFops(ctx),
        cp -> ModelHelpers.getAllHierarchyFunctionInputPorts((Component)cp.eContainer()),
        fop -> fop.getOutgoingFunctionalExchanges().stream(),
        fip -> fip.getIncomingFunctionalExchanges().stream());
  }

  private void validateProvidedInterfaces(IValidationContext ctx, Component component, Collection<IStatus> errors) {
    validatePartial(ctx, component, errors,
        getString("%provided"), //$NON-NLS-1$
        getString("%required"), //$NON-NLS-1$
        ComponentPort::getProvidedInterfaces,
        Interface::getProvidingComponentPorts,
        Interface::getRequiringComponentPorts,
        getFips(ctx),
        cp -> ModelHelpers.getAllHierarchyFunctionOutputPorts((Component)cp.eContainer()),
        fip -> fip.getIncomingFunctionalExchanges().stream(),
        fop -> fop.getOutgoingFunctionalExchanges().stream());
  }

  private Function<ComponentPort, Stream<FunctionOutputPort>> getFops(IValidationContext ctx){
    if (NO_UNUSED_EI_ON_I_WITH_ALLOCATIONS.equals(ctx.getCurrentConstraintId())) {
      return (cp) -> ModelHelpers.getAllocatedFunctionOutputPorts(cp).stream();
    } else if (NO_UNUSED_EI_ON_I_ID.equals(ctx.getCurrentConstraintId())) {
      return (cp) -> ModelHelpers.getAllHierarchyFunctionOutputPorts((Component) cp.eContainer());
    } else {
      throw new IllegalArgumentException("Unrecognized validation rule id: " + ctx.getCurrentConstraintId()); //$NON-NLS-1$
    }
  }

  private Function<ComponentPort, Stream<FunctionInputPort>> getFips(IValidationContext ctx){
    if (NO_UNUSED_EI_ON_I_WITH_ALLOCATIONS.equals(ctx.getCurrentConstraintId())) {
      return (cp) -> ModelHelpers.getAllocatedFunctionInputPorts(cp).stream();
    } else if (NO_UNUSED_EI_ON_I_ID.equals(ctx.getCurrentConstraintId())) {
      return (cp) -> ModelHelpers.getAllHierarchyFunctionInputPorts((Component) cp.eContainer());
    } else {
      throw new IllegalArgumentException("Unrecognized validation rule id: " + ctx.getCurrentConstraintId()); //$NON-NLS-1$
    }
  }

  private <FxP extends FunctionPort, FyP extends FunctionPort> void validatePartial(IValidationContext ctx, Component leftComponent, Collection<IStatus> errors, 
      String leftRole,
      String rightRole,
      Function<ComponentPort, Collection<Interface>> getInterfaces, 
      Function<Interface, Collection<ComponentPort>> getLeftComponentPorts,
      Function<Interface, Collection<ComponentPort>> getRightComponentPorts,
      Function<ComponentPort, Stream<FxP>> getAllLeftFxP,
      Function<ComponentPort, Stream<FyP>> getAllRightFyP,
      Function<FxP, Stream<FunctionalExchange>> getFxPFe,
      Function<FyP, Stream<FunctionalExchange>> getFyPFe
      ) {

    Collection<Interface> interfaces = new LinkedHashSet<Interface>();
    for (ComponentPort port : leftComponent.getContainedComponentPorts()) {      
      interfaces.addAll(getInterfaces.apply(port));
    }

    for (Interface i : interfaces) {

      // Find all cp on left component that relate to the interface here 
      Stream<ComponentPort> leftCPs = getLeftComponentPorts.apply(i).stream().filter(cp -> cp.eContainer() == leftComponent);

      Map<Component, List<ComponentPort>> rightCPs = getRightComponentPorts.apply(i).stream() // all opposite cp for interface ...
          .filter(cp -> cp.eContainer() != leftComponent) // ... which are on a different component
          .collect(Collectors.groupingBy(cp -> ((Component) cp.eContainer()))); // grouped by their parent component

      Set<FunctionalExchange> leftCandidates = leftCPs.flatMap(getAllLeftFxP).distinct().flatMap(getFxPFe).collect(Collectors.toSet());

      // Now walk over pairs [leftComponent, rightComponent]
      for (Component rightComponent : rightCPs.keySet()) {

        Set<FunctionalExchange> rightCandidates = rightCPs.get(rightComponent).stream().flatMap(getAllRightFyP).distinct().flatMap(getFyPFe).collect(Collectors.toSet());

        rightCandidates.retainAll(leftCandidates);

        for (ExchangeItem ei : i.getExchangeItems()) {
          if (isMissing(ei, rightCandidates)) {
            Collection<EObject> resultLocus = new ArrayList<EObject>();
            resultLocus.add(leftComponent);
            resultLocus.add(rightComponent);
            resultLocus.add(ei);
            resultLocus.add(i);
            resultLocus.addAll(rightCandidates);
            errors.add(ConstraintStatus.createStatus(ctx, ei, resultLocus, getMessageTemplate(ctx), i, leftRole, leftComponent, rightRole, rightComponent, ei));
          }
        }
      }
    }
  }

  private boolean isMissing(ExchangeItem ei, Collection<FunctionalExchange> candidates) {
    return isMissing(ei, candidates, FunctionalExchange::getExchangedItems);
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


