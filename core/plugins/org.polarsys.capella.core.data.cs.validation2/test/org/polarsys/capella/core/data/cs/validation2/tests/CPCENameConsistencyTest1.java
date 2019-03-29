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
import org.polarsys.capella.core.data.fa.ComponentExchange;
import org.polarsys.capella.core.data.fa.ComponentPort;
import org.polarsys.capella.core.model.skeleton.CapellaModelSkeleton;

public class CPCENameConsistencyTest1 extends DynamicValidationTest {

  ComponentTemplate1 template1;
  
  @Override
  protected void initModel(CapellaModelSkeleton skeleton) {
    template1 = new ComponentTemplate1(skeleton, this);
  }

  @SuppressWarnings("nls")
  @Override
  public void test() throws Exception {
    
    executeCommand(() -> {
      template1.cp1.setName("cp1");
      template1.cp2.setName("cp2");
    });


    // no CE -> ok
    assertThat(validateCP(template1.cp1), is(okStatus()));
    
    // one CE => must have same name
    executeCommand(() -> {
      ComponentExchange ce = (ComponentExchange) create(template1.component, CE);
      ce.setName("ce1");
      ce.setSource(template1.cp1);
      ce.setTarget(template1.cp2);
    });
    
    assertThat(validateCP(template1.cp1), is(not(okStatus())));
    assertThat(validateCP(template1.cp2), is(not(okStatus())));
    
    executeCommand(() -> {
      template1.cp1.setName("ce1");
    });

    assertThat(validateCP(template1.cp1), is((okStatus())));
    assertThat(validateCP(template1.cp2), is(not(okStatus())));
    
    executeCommand(() -> {
      template1.cp2.setName("ce1");
    });
    
    assertThat(validateCP(template1.cp1), is((okStatus())));
    assertThat(validateCP(template1.cp1), is((okStatus())));
    
    // Add a 2nd FE -> Name doesnt matter
    executeCommand(() -> {
      ComponentExchange ce2 = (ComponentExchange) create(template1.component, CE);
      ce2.setName("ce2");
      ce2.setSource(template1.cp1);
      ce2.setTarget(template1.cp2);
      template1.cp1.setName("cp1");
      template1.cp2.setName("cp2");
    });
    
    assertThat(validateCP(template1.cp1), is((okStatus())));
    assertThat(validateCP(template1.cp2), is((okStatus())));
    
  }

  private IStatus validateCP(ComponentPort cp) {
    return ValidationHelpers.validate(cp, ExchangePortNameConsistency.ID_CP_CE);
   }
  
  
  
}

