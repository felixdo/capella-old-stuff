package org.polarsys.capella.core.data.cs.validation2.tests;

import static org.junit.Assert.assertArrayEquals;

import org.polarsys.capella.core.data.cs.validation2.ModelHelpers;
import org.polarsys.capella.core.data.cs.validation2.tests.common.DynamicValidationTest;
import org.polarsys.capella.core.data.la.LaPackage;
import org.polarsys.capella.core.data.la.LogicalComponent;
import org.polarsys.capella.core.data.la.LogicalComponentPkg;
import org.polarsys.capella.core.model.skeleton.CapellaModelSkeleton;

public class TestGetComponentHierarchy extends DynamicValidationTest {

  LogicalComponent root;
  LogicalComponent lc1;
  LogicalComponent lc1_1;
  LogicalComponent lc1_2;
  LogicalComponent lc2;
  LogicalComponent lc2_1;
  
  @Override
  protected void initModel(CapellaModelSkeleton skeleton2) {
    root = skeleton.getLogicalArchitecture().getOwnedLogicalComponent();
    lc1 = create(root, LC);
    lc2 = create(root, LC);
    lc1_1 = create(lc1, LC);
    lc1_2 = create(lc1, LC);
    lc2_1 = create(lc2, LC);  
  }

  @Override
  public void test() throws Exception {

    Object[] actual = ModelHelpers.getComponentHierarchy(skeleton.getLogicalArchitecture().getOwnedLogicalComponent()).toArray();
    Object[] expected = new Object[] { root, lc1, lc2, lc1_1, lc1_2, lc2_1 };
    
    assertArrayEquals(expected, actual);
    
    // the confusing aspect of part/type hierarchy is that capella creates component and type as siblings. the location of the component itself however does not 
    // matter:
    
    executeCommand(() -> {
      LogicalComponentPkg pkg = create(root, LaPackage.Literals.LOGICAL_COMPONENT_PKG);
      pkg.getOwnedLogicalComponents().add(lc1);
      pkg.getOwnedLogicalComponents().add(lc2);
      pkg.getOwnedLogicalComponents().add(lc1_1);
      pkg.getOwnedLogicalComponents().add(lc1_2);
      pkg.getOwnedLogicalComponents().add(lc2);
      pkg.getOwnedLogicalComponents().add(lc2_1);
    });

    assertArrayEquals(expected, actual);

  }

  
  
  
  
  
}
