package org.polarsys.capella.core.data.cs.validation2.tests.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.validation.model.EvaluationMode;
import org.eclipse.emf.validation.model.IConstraintStatus;
import org.eclipse.emf.validation.service.ConstraintRegistry;
import org.eclipse.emf.validation.service.IConstraintDescriptor;
import org.eclipse.emf.validation.service.IConstraintFilter;
import org.eclipse.emf.validation.service.IValidator;
import org.eclipse.emf.validation.service.ModelValidationService;

public class ValidationHelpers {

  static {
    ModelValidationService.getInstance().loadXmlConstraintDeclarations();
  }

  /**
   * Runs model validation on the given EObject. Before running the validation,
   * we install a filter on the validator to only validate the constraints
   * with the given rule IDs.
   * @param e
   * @return the status we receive from the model validation service after validation.
   */
  public static IStatus validate(EObject e, String... ruleIDs) {
    // force activation of constraints under test

    ConstraintRegistry registry = ConstraintRegistry.getInstance();    
    Map<IConstraintDescriptor,Boolean> enabledState = new HashMap<>();

    try {
      for (String id : ruleIDs) {
        IConstraintDescriptor descriptor = registry.getDescriptor(id);
        assertNotNull("Did not find constraint with id " + id //$NON-NLS-1$ 
            + "in the constraint registry", descriptor); //$NON-NLS-1$
        assertFalse(descriptor.isError());
        enabledState.put(descriptor, descriptor.isEnabled());
        descriptor.setEnabled(true);
      }
      IValidator<EObject> validator = ModelValidationService.getInstance().newValidator(EvaluationMode.BATCH);
      validator.addConstraintFilter(new IConstraintFilter() {
        public boolean accept(IConstraintDescriptor constraint_p, EObject target_p) {
          for (String id : ruleIDs) {
            if (id.equals(constraint_p.getId())) {
              return true;
            }
          }
          return false;
        }
      });
      IStatus result = validator.validate(e);
      for (IConstraintDescriptor descriptor : enabledState.keySet()) {
        assertFalse(descriptor.isError());
      }
      return result;
    } finally {
      for (Map.Entry<IConstraintDescriptor, Boolean> entry : enabledState.entrySet()) {
        entry.getKey().setEnabled(entry.getValue());
      }
    }
  }

  

  /**
   * Search the given validation result for a constraint status targeting a specific object
   * @param target
   * @param result
   * @return
   */
  public static IConstraintStatus find(EObject target, IStatus result) {    
    if (result.isMultiStatus()) {
      for (IStatus child : result.getChildren()) {
        IConstraintStatus ics = find(target, child);
        if (ics != null) {
          return ics;
        }
      }
      return null;
    } else if (result instanceof IConstraintStatus && ((IConstraintStatus) result).getTarget() == target) {
      return (IConstraintStatus) result;
    } else {
      return null;
    }
  }
  
}
