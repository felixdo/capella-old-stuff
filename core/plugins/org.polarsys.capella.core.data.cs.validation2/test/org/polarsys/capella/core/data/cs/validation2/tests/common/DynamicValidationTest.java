package org.polarsys.capella.core.data.cs.validation2.tests.common;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.validation.service.ModelValidationService;
import org.eclipse.ui.statushandlers.StatusManager;

public abstract class DynamicValidationTest extends BasicDynamicModelTest {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    ModelValidationService.getInstance().loadXmlConstraintDeclarations();
  }


  /**
   * Validate the given Object with the EMF Validation service.
   * Triggers a failure unless the result status 'isOK()'. 
   * 
   * @param e
   */
  protected IStatus ok(EObject e, String ruleID) {
    IStatus status = ValidationHelpers.validate(e, ruleID);
    if (!status.isOK()) {
      StatusManager.getManager().handle(status, StatusManager.LOG);
      fail(status.getMessage());
    }
    return status;
  }
  
  protected void ok(IStatus s) {
    assertTrue(s.isOK());
  }
  
  protected void ko(IStatus s) {
    assertFalse(s.isOK());
  }

  /**
   * Validate the given Object with the EMF Validation service.
   * Triggers a failure if the result status 'isOK()'.
   * @param e
   */
  protected IStatus ko(EObject e, String ruleID) {
    IStatus status = ValidationHelpers.validate(e, ruleID);
    assertFalse("Validator returned unexpected OK Status", status.isOK()); //$NON-NLS-1$
    return status;
  }
  
}
