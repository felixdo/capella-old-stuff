package org.polarsys.capella.core.data.cs.validation2.tests;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.polarsys.capella.core.data.cs.validation2.FE_I_ExchangeItemConsistency.NO_UNUSED_EI_ON_FE_WITH_ALLOCATIONS_ID;
import static org.polarsys.capella.core.data.cs.validation2.tests.common.StatusMatcher.okStatus;
import static org.polarsys.capella.core.data.cs.validation2.helpers.Allocators.allocate;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.cs.validation2.tests.common.ValidationHelpers;
import org.polarsys.capella.core.data.fa.ComponentPort;
import org.polarsys.capella.core.data.fa.FunctionalExchange;

/**
 * Check that FE-I rule does not generate errors for exchange items on internal FE.
 */
public class FE_I_IgnoreInternalFE extends IFEAllocationTestTemplate {

  FunctionalExchange internalFE;

  @Override
  public void test() throws Exception {

    executeCommand(() -> {
      internalFE = create(center.pf1.eContainer(), FE);
      internalFE.setSource(center.fop1_1);
      internalFE.setTarget(center.fip2_1);
      allocate(ei1).on(internalFE);
    });

    ok();
    
    ComponentPort requirerCp = center.cp1;
    ComponentPort providerCp = center.cp2;
    executeCommand(() -> {
      providerCp.getProvidedInterfaces().add(i2);
      requirerCp.getRequiredInterfaces().add(i2);
    });

    ok();

    executeCommand(() -> {
      allocate(center.fop1_1).on(requirerCp);
      allocate(center.fip2_1).on(providerCp);
    });

    ok();
 
    // see what happens if we deallocate the source or target function and validate again.
    executeCommand(()->{
      EcoreUtil.delete(center.pf1.getComponentFunctionalAllocations().get(0));
    });
    ok();
    undo();
    
    // see what happens if we deallocate the source or target function and validate again.
    executeCommand(()->{
      EcoreUtil.delete(center.pf2.getComponentFunctionalAllocations().get(0));
    });    
    ok();

  }

  private void ok() {
    assertThat(unusedEIOnFunctionalExchanges(center.component), is(okStatus()));
  }

  protected IStatus unusedEIOnFunctionalExchanges(Component c){
    return ValidationHelpers.validate(c, NO_UNUSED_EI_ON_FE_WITH_ALLOCATIONS_ID);//, NO_UNUSED_EI_ON_FE_ID);
  }

}
