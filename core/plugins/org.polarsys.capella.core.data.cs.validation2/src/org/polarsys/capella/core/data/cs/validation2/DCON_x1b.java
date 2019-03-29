package org.polarsys.capella.core.data.cs.validation2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.validation.IValidationContext;
import org.eclipse.emf.validation.model.ConstraintStatus;
import org.polarsys.capella.core.data.capellacore.Feature;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.cs.Interface;
import org.polarsys.capella.core.data.fa.ComponentPort;
import org.polarsys.capella.core.data.fa.FunctionInputPort;
import org.polarsys.capella.core.data.fa.FunctionOutputPort;
import org.polarsys.capella.core.data.fa.FunctionPort;
import org.polarsys.capella.core.data.information.ExchangeItem;

public class DCON_x1b extends AbstractComponentValidationRule {

  public static final String RULE_ID = "org.polarsys.capella.core.data.cs.validation2.DCON_x1b"; //$NON-NLS-1$

  
  protected void validateComponent(IValidationContext ctx, Component component, Collection<IStatus> errors) {

    for (ComponentPort port : getComponentPorts(component)) {

      Collection<FunctionInputPort> fips = new ArrayList<>();
      Collection<FunctionOutputPort> fops = new ArrayList<>();
      ModelHelpers.splitPorts(port.getAllocatedFunctionPorts(), fips, fops);

      for (FunctionOutputPort fop : fops) {
        for (ExchangeItem ei : findMissing(fop, FunctionOutputPort::getOutgoingExchangeItems, port.getRequiredInterfaces())) {
          makeError(ctx, component, errors, port, fop, ei, port.getRequiredInterfaces());
        }
      }
      
      for (FunctionInputPort fip : fips) {
        for (ExchangeItem ei : findMissing(fip, FunctionInputPort::getIncomingExchangeItems, port.getProvidedInterfaces())) {
          makeError(ctx, component, errors, port, fip, ei, port.getProvidedInterfaces());
        }
      }

    }

  }

  private <FxP extends FunctionPort> Collection<ExchangeItem> findMissing(FxP fxp, Function<FxP, Collection<ExchangeItem>> getExchangeItems, Collection<Interface> interfaces) {
    Collection<ExchangeItem> missingOnInterface = new ArrayList<ExchangeItem>();
    for (ExchangeItem ei : getExchangeItems.apply(fxp)) {
      boolean missing = true;
      for (Interface i : interfaces) {
        if (i.getExchangeItems().contains(ei)) {
          missing = false;
          break;
        }
      }
      if (missing) {
        missingOnInterface.add(ei);
      }
    }
    return missingOnInterface;
  }

  private void makeError(IValidationContext ctx, Component component, Collection<IStatus> errors, ComponentPort port,
      FunctionPort fp, ExchangeItem ei, Collection<Interface> relatedInterfaces) {
    Collection<EObject> resultLocus = new ArrayList<EObject>();
    resultLocus.add(component);
    resultLocus.add(port);
    resultLocus.add(fp);
    resultLocus.add(ei);
    resultLocus.addAll(relatedInterfaces);
    errors.add(ConstraintStatus.createStatus(ctx, ei, resultLocus, IStatus.WARNING, 3, getMessageTemplate(ctx),
        ei.getName(), fp.getName(), component.getName(), port.getName()));
  }

  private Collection<ComponentPort> getComponentPorts(Component component){
    Collection<ComponentPort> result = new ArrayList<>();
    for (Feature f : component.getOwnedFeatures()) {
      if (f instanceof ComponentPort) {
        result.add((ComponentPort) f);
      }
    }
    return result;
  }

}
