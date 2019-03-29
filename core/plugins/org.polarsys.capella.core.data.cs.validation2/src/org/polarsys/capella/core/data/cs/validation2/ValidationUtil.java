package org.polarsys.capella.core.data.cs.validation2;

import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.validation.IValidationContext;
import org.osgi.framework.Bundle;

class ValidationUtil {

  final Bundle bundle;
  
  public ValidationUtil(Bundle bundle) {
    this.bundle = bundle;
  }
  
  String getString(String key) {
    return Platform.getResourceString(bundle, key);
  }

  String getMessageTemplate(IValidationContext ctx) {
    int lastDot = ctx.getCurrentConstraintId().lastIndexOf('.');
    StringBuilder key = new StringBuilder("%"); //$NON-NLS-1$
    key.append(ctx.getCurrentConstraintId().substring(lastDot+1).toLowerCase());
    key.append("_message"); //$NON-NLS-1$
    return getString(key.toString());
  }

}
