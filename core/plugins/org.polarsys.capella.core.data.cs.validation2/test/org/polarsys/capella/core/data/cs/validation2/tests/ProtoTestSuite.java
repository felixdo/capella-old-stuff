package org.polarsys.capella.core.data.cs.validation2.tests;

import org.polarsys.capella.core.data.cs.validation2.tests.common.ReflectiveValidationTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ProtoTestSuite extends TestSuite {

  @SuppressWarnings("nls")
  public ProtoTestSuite() {
    super("ProtoTestSuite");
    addTest(new ReflectiveValidationTest("org.polarsys.capella.core.data.cs.validation2.DCOM_x1", "DCOM_x1"));
    addTest(new ReflectiveValidationTest("org.polarsys.capella.core.data.cs.validation2.DCOM_x5", "DCOM_x5"));
    addTest(new ReflectiveValidationTest("org.polarsys.capella.core.data.cs.validation2.DCON_x1a", "DCON_x1a"));
    addTest(new ReflectiveValidationTest("org.polarsys.capella.core.data.cs.validation2.DCON_x1b", "DCON_x1b"));
    addTest(new IFEAllocationTest1());
    addTest(new IFEAllocationTest2());
    addTest(new IFEAllocationTest4());
    addTest(new FE_I_IgnoreInternalFE());
    addTest(new FE_I_With_PotentialInterface_Exchange());
    addTest(new FxPFEAllocationTest1());
    addTest(new FxPFENameConsistencyTest());
    addTest(new CPCENameConsistencyTest1());
    addTest(new PPPLNameConsistencyTest());
    addTest(new TestGetComponentHierarchy());
  }

  public static Test suite() {
    return new ProtoTestSuite();
  }

}
