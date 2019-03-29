package org.polarsys.capella.core.data.cs.validation2.tests;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.polarsys.capella.core.data.cs.validation2.tests.common.StatusMatcher.okStatus;

import org.eclipse.core.runtime.IStatus;
import org.polarsys.capella.core.data.cs.validation2.ExchangePortNameConsistency;
import org.polarsys.capella.core.data.cs.validation2.tests.common.ComponentTemplate1;
import org.polarsys.capella.core.data.cs.validation2.tests.common.DynamicValidationTest;
import org.polarsys.capella.core.data.cs.validation2.tests.common.ValidationHelpers;
import org.polarsys.capella.core.data.fa.FunctionPort;
import org.polarsys.capella.core.data.fa.FunctionalExchange;
import org.polarsys.capella.core.model.skeleton.CapellaModelSkeleton;

public class FxPFENameConsistencyTest extends DynamicValidationTest {

  ComponentTemplate1 template1;
  
  @Override
  protected void initModel(CapellaModelSkeleton skeleton) {
    template1 = new ComponentTemplate1(skeleton, this);
  }

  @SuppressWarnings("nls")
  @Override
  public void test() throws Exception {
    
    executeCommand(() -> {      
      template1.fip1_1.setName("fip1");
      template1.fop1_1.setName("fop1");
    });
    
    // no FE -> ok
    assertThat(validateFxP(template1.fip1_1), is(okStatus()));
    assertThat(validateFxP(template1.fop1_1), is(okStatus()));
    
    // one FE => must have same name
    executeCommand(() -> {
      FunctionalExchange fe = (FunctionalExchange) create(template1.pf1.eContainer(), FE);
      fe.setName("fe1");
      fe.setSource(template1.fop1_1);
      fe.setTarget(template1.fip1_1);
    });
    
    assertThat(validateFxP(template1.fip1_1), is(not(okStatus())));
    assertThat(validateFxP(template1.fop1_1), is(not(okStatus())));
    
    executeCommand(() -> {
      template1.fip1_1.setName("fe1");
    });
    
    assertThat(validateFxP(template1.fip1_1), is((okStatus())));
    assertThat(validateFxP(template1.fop1_1), is(not(okStatus())));
    
    executeCommand(() -> {
      template1.fop1_1.setName("fe1");
    });
    
    assertThat(validateFxP(template1.fip1_1), is((okStatus())));
    assertThat(validateFxP(template1.fop1_1), is((okStatus())));
    
    // Add a 2nd FE -> Name doesnt matter
    executeCommand(() -> {
      FunctionalExchange fe2 = (FunctionalExchange) create(template1.pf1.eContainer(), FE);
      fe2.setName("fe2");
      fe2.setSource(template1.fop1_1);
      fe2.setTarget(template1.fip1_1);
      template1.fop1_1.setName("fop1");
      template1.fip1_1.setName("fip1");
    });
    
    assertThat(validateFxP(template1.fip1_1), is((okStatus())));
    assertThat(validateFxP(template1.fop1_1), is((okStatus())));

  }

  
  private IStatus validateFxP(FunctionPort fxp) {
   return ValidationHelpers.validate(fxp, ExchangePortNameConsistency.ID_FXP_FE);
  }

}

