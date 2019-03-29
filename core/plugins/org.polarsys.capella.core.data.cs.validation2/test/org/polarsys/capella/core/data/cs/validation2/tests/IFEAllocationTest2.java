package org.polarsys.capella.core.data.cs.validation2.tests;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.polarsys.capella.core.data.cs.validation2.FE_I_ExchangeItemConsistency.NO_UNUSED_EI_ON_FE_ID;
import static org.polarsys.capella.core.data.cs.validation2.FE_I_ExchangeItemConsistency.NO_UNUSED_EI_ON_FE_WITH_ALLOCATIONS_ID;
import static org.polarsys.capella.core.data.cs.validation2.I_FE_ExchangeItemConsistency.NO_UNUSED_EI_ON_I_ID;
import static org.polarsys.capella.core.data.cs.validation2.I_FE_ExchangeItemConsistency.NO_UNUSED_EI_ON_I_WITH_ALLOCATIONS;
import static org.polarsys.capella.core.data.cs.validation2.tests.common.StatusMatcher.okStatus;
import static org.polarsys.capella.core.model.helpers.Allocators.allocate;

import org.eclipse.core.runtime.IStatus;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.cs.validation2.tests.common.ValidationHelpers;
import org.polarsys.capella.core.data.fa.ComponentPort;
import org.polarsys.capella.core.data.fa.FunctionalExchange;

/**
 * Test I-FE allocation where center component shares interface with 2 other components.
 * Shows that FE are grouped by component pair, and at least one FE in every group must provide
 * the exchange items of interface.
 */
public class IFEAllocationTest2 extends IFEAllocationTestTemplate {
  
  FunctionalExchange center_left;
  FunctionalExchange center_right;

  @Override
  public void test() throws Exception {

    ComponentPort requirerCp = center.cp1;
    ComponentPort leftProviderCp = left.cp1;
    ComponentPort rightPrioviderCp = right.cp1;
    
    executeCommand(() -> {
      leftProviderCp.getProvidedInterfaces().add(i1);
      rightPrioviderCp.getProvidedInterfaces().add(i1);
      requirerCp.getRequiredInterfaces().add(i1);
      center_left = create(center.pf1.eContainer(), FE);
      center_left.setSource(center.fop1_1);
      center_left.setTarget(left.fip1_1);
      center_right = create(center.pf1.eContainer(), FE);
      center_right.setSource(center.fop1_2);
      center_right.setTarget(right.fip1_1);
    });

    // ei1 and ei2 are on i1, but on no fe
    IStatus cs = unusedEIOnInterfaces(center.component, false);
    assertThat(cs, is(not(okStatus())));

    // it's not sufficient to allocate the ei on either fe
    executeCommand(
        () -> 
        allocate(ei1, ei2).on(center_left)
    );
    cs = unusedEIOnInterfaces(center.component, false);
    assertThat(cs, is(not(okStatus())));
    undo();
    
    executeCommand(
        () -> 
        allocate(ei1, ei2).on(center_right)
    );
    
    cs = unusedEIOnInterfaces(center.component, false);
    assertThat(cs, is(not(okStatus())));
    undo();

    // they must be on both fe, since they involve different component pairs
    executeCommand(() -> 
      allocate(ei1, ei2).on(center_left)
                        .on(center_right)
    );

    cs = unusedEIOnInterfaces(center.component, false);
    assertThat(cs, is(okStatus()));

  }

  protected IStatus unusedEIOnInterfaces(Component c, boolean considerAllocations){
    return ValidationHelpers.validate(c, considerAllocations ? NO_UNUSED_EI_ON_I_WITH_ALLOCATIONS : NO_UNUSED_EI_ON_I_ID);
  }

  protected IStatus unusedEIOnFunctionalExchanges(Component c, boolean considerAllocations){
    return ValidationHelpers.validate(c, considerAllocations ? NO_UNUSED_EI_ON_FE_WITH_ALLOCATIONS_ID : NO_UNUSED_EI_ON_FE_ID);
  }

}
