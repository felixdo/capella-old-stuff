package org.polarsys.capella.core.data.cs.validation2;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.emf.validation.model.IConstraintStatus;
import org.eclipse.osgi.util.NLS;
import org.polarsys.capella.common.helpers.validation.ConstraintStatusDiagnostic;
import org.polarsys.capella.common.tools.report.appenders.reportlogview.MarkerViewHelper;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.validation.ui.ide.quickfix.AbstractCapellaMarkerResolution;

public class XPExchangeNameQuickfix extends AbstractCapellaMarkerResolution {

  @Override
  public void run(IMarker marker) {

    Diagnostic diag = marker.getAdapter(Diagnostic.class);
    if (diag instanceof ConstraintStatusDiagnostic) {
      IConstraintStatus constraintStatus = ((ConstraintStatusDiagnostic) diag).getConstraintStatus();

      final NamedElement target = (NamedElement) constraintStatus.getTarget();
      for (EObject e : constraintStatus.getResultLocus()) {
        if (e != target) {
          final NamedElement reference = (NamedElement) e;
          TransactionalEditingDomain domain = TransactionUtil.getEditingDomain(target);
          Command c = new RecordingCommand(TransactionUtil.getEditingDomain(target), 
              NLS.bind(Activator.getResourceString("%xp_exchange_name_quickfix"), reference.getName())) //$NON-NLS-1$
              {
            @Override
            protected void doExecute() {
              target.setName(reference.getName());
            }
              };
              domain.getCommandStack().execute(c);
              try {
                marker.delete();
              } catch (CoreException e1) {
                Platform.getLog(Activator.getContext().getBundle()).log(new Status(IStatus.ERROR, Activator.getContext().getBundle().getSymbolicName(), e1.getMessage(), e1));
              }
        }
      }
    }
  }

  protected boolean canResolve(IMarker marker) {
    String ruleId = MarkerViewHelper.getRuleID(marker, true);
    return ExchangePortNameConsistency.ID_CP_CE.equals(ruleId) ||
        ExchangePortNameConsistency.ID_FXP_FE.equals(ruleId) ||
        ExchangePortNameConsistency.ID_PP_PL.equals(ruleId);
  }

}
