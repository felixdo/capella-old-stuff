package org.polarsys.capella.core.data.cs.validation2.tests;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.polarsys.capella.core.data.cs.validation2.tests.common.StatusMatcher.okStatus;

import org.eclipse.core.runtime.IStatus;
import org.polarsys.capella.core.data.cs.PhysicalLink;
import org.polarsys.capella.core.data.cs.PhysicalPort;
import org.polarsys.capella.core.data.cs.validation2.ExchangePortNameConsistency;
import org.polarsys.capella.core.data.cs.validation2.tests.common.ComponentTemplate1;
import org.polarsys.capella.core.data.cs.validation2.tests.common.DynamicValidationTest;
import org.polarsys.capella.core.data.cs.validation2.tests.common.ValidationHelpers;
import org.polarsys.capella.core.model.skeleton.CapellaModelSkeleton;

public class PPPLNameConsistencyTest extends DynamicValidationTest {

  ComponentTemplate1 template1;
  
  @Override
  protected void initModel(CapellaModelSkeleton skeleton) {
    template1 = new ComponentTemplate1(skeleton, this);
  }

  @SuppressWarnings("nls")
  @Override
  public void test() throws Exception {
    
    executeCommand(() -> {      
      template1.pp1.setName("pp1"); 
      template1.pp2.setName("pp2");
    });
    
    // no PL -> ok
    assertThat(validatePP(template1.pp1), is(okStatus()));
    assertThat(validatePP(template1.pp2), is(okStatus()));
    
    // one PL => must have same name
    executeCommand(() -> {
      PhysicalLink pl = (PhysicalLink) create(template1.component, PL);
      pl.setName("pl1");
      pl.getLinkEnds().add(template1.pp1);
      pl.getLinkEnds().add(template1.pp2);
    });
    
    assertThat(validatePP(template1.pp1), is(not(okStatus())));
    assertThat(validatePP(template1.pp2), is(not(okStatus())));
    
    executeCommand(() -> {
      template1.pp1.setName("pl1");
    });
    
    assertThat(validatePP(template1.pp1), is((okStatus())));
    assertThat(validatePP(template1.pp2), is(not(okStatus())));
    
    executeCommand(() -> {
      template1.pp2.setName("pl1");
    });
    
    assertThat(validatePP(template1.pp1), is((okStatus())));
    assertThat(validatePP(template1.pp2), is((okStatus())));
    
    // Add a 2nd PL -> Name doesnt matter
    executeCommand(() -> {
      PhysicalLink pl2 = (PhysicalLink) create(template1.component, PL);
      pl2.setName("pl2");
      pl2.getLinkEnds().add(template1.pp1);
      pl2.getLinkEnds().add(template1.pp2);
      template1.pp1.setName("pp1");
      template1.pp2.setName("pp2");
    });

    assertThat(validatePP(template1.pp1), is((okStatus())));
    assertThat(validatePP(template1.pp2), is((okStatus())));
  }

  private IStatus validatePP(PhysicalPort pp) {
    return ValidationHelpers.validate(pp, ExchangePortNameConsistency.ID_PP_PL);
  }

}

