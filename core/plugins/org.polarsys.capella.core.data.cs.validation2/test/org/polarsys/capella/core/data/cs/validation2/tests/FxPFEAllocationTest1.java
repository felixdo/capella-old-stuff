package org.polarsys.capella.core.data.cs.validation2.tests;

import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.cs.validation2.FE_FxP_ExchangeItemConsistency;
import org.polarsys.capella.core.data.cs.validation2.tests.common.ValidationHelpers;
import org.polarsys.capella.core.data.fa.FunctionalExchange;
import org.polarsys.capella.core.model.helpers.FunctionalExchangeExt;

import static org.polarsys.capella.core.data.cs.validation2.tests.common.StatusMatcher.okStatus;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import static org.polarsys.capella.core.data.cs.validation2.helpers.Allocators.allocate;


import org.eclipse.core.runtime.IStatus;

/**
 * This tests the rules FE_FxP and its inverse FxP_FE:
 * - An EI on an FxP must be on any of its interfacing FE.
 * - An EI on an interfacing FE must be on its connected FxP.
 */
public class FxPFEAllocationTest1 extends IFEAllocationTestTemplate {

  FunctionalExchange fe1;

  @Override
  public void test() throws Exception {

    // create an internal functional exchange    
    executeCommand(() -> {
      fe1 = FunctionalExchangeExt.createFunctionalExchange(left.fop1_1, left.fip2_1);
      allocate(ei1).on(left.fop1_1);
    });

    // finds ei1 on left.fop1_1 which has no interfacing exchanges => ok, covered by new rule
    assertThat(unusedFxPExchangeItems(left.component), is(okStatus()));

    // unused ei on the internal exchange is no problem
    executeCommand(() -> allocate(ei2).on(fe1));
    assertThat(unusedFEExchangeItems(left.component), is(okStatus()));

    // now make that exchange interfacing by connecting it to the center component
    executeCommand(() -> fe1.setTarget(center.fip1_1));

    // the unused FE EI is reported, no matter if the source or target component is validated
    assertThat(unusedFEExchangeItems(left.component), is(not(okStatus())));
    assertThat(unusedFEExchangeItems(center.component), is(not(okStatus())));

    // but the unused FxP EI is only detected on the side that allocates the FxP:
    assertThat(unusedFxPExchangeItems(left.component), is(not(okStatus())));
    assertThat(unusedFxPExchangeItems(center.component), is(okStatus()));

    // this fixes the existing violations
    executeCommand(() -> {
      allocate(ei1).on(fe1);
      allocate(ei2).on(left.fop1_1);
    });
    
    // ei1,ei2 are now on fe and left fxp 
    
    // ... validating the left side is ok now
    assertThat(unusedFEExchangeItems(left.component), is(okStatus()));
    assertThat(unusedFxPExchangeItems(left.component), is(okStatus()));
    
    // but not the right side
    assertThat(unusedFEExchangeItems(center.component), is(not(okStatus())));
    
    // to fix that, one has to allocate both fe ei on the center fxp
    executeCommand(() -> allocate(ei1, ei2).on(center.fip1_1));
    assertThat(unusedFEExchangeItems(center.component), is(okStatus()));

    // test that FxP validation works also on FIP
    executeCommand(() -> allocate(ei3).on(center.fip1_1));

    // only the affected fxp is impacted, not the other side
    assertThat(unusedFxPExchangeItems(center.component), is(not(okStatus())));

    // now allocate ei3 on the fe
    executeCommand(() -> allocate(ei3).on(fe1));
    
    // ei3 no longer unused on the center fxp
    assertThat(unusedFxPExchangeItems(center.component), is(okStatus())); 
  }
  
  
  private IStatus unusedFxPExchangeItems(Component c) {
    return ValidationHelpers.validate(c, FE_FxP_ExchangeItemConsistency.UNUSED_FXP_EI_ID);
  }

  private IStatus unusedFEExchangeItems(Component c) {
    return ValidationHelpers.validate(c, FE_FxP_ExchangeItemConsistency.UNUSED_FE_EI_ID);
  }

}
