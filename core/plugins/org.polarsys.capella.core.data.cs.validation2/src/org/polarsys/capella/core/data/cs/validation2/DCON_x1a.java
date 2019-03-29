package org.polarsys.capella.core.data.cs.validation2;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.validation.IValidationContext;
import org.eclipse.emf.validation.model.ConstraintStatus;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.cs.Interface;
import org.polarsys.capella.core.data.fa.ComponentPort;
import org.polarsys.capella.core.data.fa.FaPackage;
import org.polarsys.capella.core.data.fa.FunctionInputPort;
import org.polarsys.capella.core.data.fa.FunctionOutputPort;
import org.polarsys.capella.core.data.fa.FunctionPort;
import org.polarsys.capella.core.data.information.ExchangeItem;

public class DCON_x1a extends AbstractComponentValidationRule {

  public static final String RULE_ID = "org.polarsys.capella.core.data.cs.validation2.DCON_x1a"; //$NON-NLS-1$

  /**
   * Internal code used by the quickfix provider
   */
  public static final int FIPS = 1;

  /**
   * Internal code used by the quickfix provider
   */
  public static final int FOPS = 2;

  protected void validateComponent(IValidationContext ctx, Component component, Collection<IStatus> errors) {

    for (ComponentPort port : component.getContainedComponentPorts()) {

      Collection<FunctionInputPort> fips = new ArrayList<>();
      Collection<FunctionOutputPort> fops = new ArrayList<>();
      ModelHelpers.splitPorts(port.getAllocatedFunctionPorts(), fips, fops);

      for (Interface i : port.getProvidedInterfaces()) {
        for (ExchangeItem ei : findMissing(i, fips, FaPackage.Literals.FUNCTION_INPUT_PORT__INCOMING_EXCHANGE_ITEMS)) {
          makeError(ctx, component, errors, port, i, ei, FIPS);
        }
      }

      for (Interface i : port.getRequiredInterfaces()) {
        for (ExchangeItem ei : findMissing(i, fops, FaPackage.Literals.FUNCTION_OUTPUT_PORT__OUTGOING_EXCHANGE_ITEMS)) {
          makeError(ctx, component, errors, port, i, ei, FOPS);
        }
      }

    }
  }

  private void makeError(IValidationContext ctx, Component component, Collection<IStatus> errors, ComponentPort port,
      Interface i, ExchangeItem ei, int code) {
    Collection<EObject> resultLocus = new ArrayList<EObject>();
    resultLocus.add(ei);
    resultLocus.add(component);
    resultLocus.add(i);
    resultLocus.add(port);
    // instead of adding the allocated fips/fops to the locus here, use a specific code that's consumed in
    // the quickfix provider. this is to reduce the number of elements in the 'goto' menu. 
    // also see https://bugs.polarsys.org/show_bug.cgi?id=2437
    errors.add(ConstraintStatus.createStatus(ctx, ei, resultLocus, IStatus.WARNING, code, getMessageTemplate(ctx),
        ei.getName(), i.getName(), component.getName(), port.getName()));
  }

  private Collection<ExchangeItem> findMissing(Interface i, Collection<? extends FunctionPort> fps, EStructuralFeature exchangeItemsFeature) {
    Collection<ExchangeItem> result = new ArrayList<>();        
    for (ExchangeItem iEI : i.getExchangeItems()) {
      boolean missingOnFop = true;
      for (FunctionPort fp : fps) {
        if (((Collection<?>)(fp.eGet(exchangeItemsFeature))).contains(iEI)){
          missingOnFop = false;
          break; 
        }
      }
      if (missingOnFop) {
        result.add(iEI);
      }
    }
    return result;
  }

}
