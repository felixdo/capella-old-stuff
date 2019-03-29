package org.polarsys.capella.core.data.cs.validation2;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.validation.EMFEventType;
import org.eclipse.emf.validation.IValidationContext;
import org.eclipse.emf.validation.model.ConstraintStatus;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.validation.rule.AbstractValidationRule;

public abstract class AbstractComponentValidationRule extends AbstractValidationRule {

  final ValidationUtil util = new ValidationUtil(Activator.getContext().getBundle());

  @Override
  public IStatus validate(IValidationContext ctx) {
    IStatus result = null;
    if (ctx.getEventType() == EMFEventType.NULL) {
      
      Collection<IStatus> errors = new ArrayList<IStatus>();
      validateComponent(ctx, (Component) ctx.getTarget(), errors);
      
      return errors.size() > 0 ? ConstraintStatus.createMultiStatus(ctx, errors) : ctx.createSuccessStatus();
      
    } else { 
      result = ctx.createSuccessStatus();
    }
    return result;
  }

  protected abstract void validateComponent(IValidationContext ctx, Component component, Collection<IStatus> errors);  

  
  protected String getString(String key) {
    return util.getString(key);
  }
  
  protected String getMessageTemplate(IValidationContext ctx) {
    return util.getMessageTemplate(ctx);
  }
}
