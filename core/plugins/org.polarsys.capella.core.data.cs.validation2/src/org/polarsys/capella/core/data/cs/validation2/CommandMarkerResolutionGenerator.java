package org.polarsys.capella.core.data.cs.validation2;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IMarker;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;
import org.polarsys.capella.common.helpers.validation.ConstraintStatusDiagnostic;

public class CommandMarkerResolutionGenerator implements IMarkerResolutionGenerator {

  private final CommandResolutionGenerator commandGenerator;
  public CommandMarkerResolutionGenerator(CommandResolutionGenerator commandGenerator) {
    this.commandGenerator = commandGenerator;
  }
  @Override
  public IMarkerResolution[] getResolutions(IMarker marker) {
    Collection<IMarkerResolution> result = new ArrayList<IMarkerResolution>();
    Diagnostic diag = marker.getAdapter(Diagnostic.class);
    if (diag instanceof ConstraintStatusDiagnostic) {
      ConstraintStatusDiagnostic csd = (ConstraintStatusDiagnostic) diag;
      for (Command c : commandGenerator.generateResolutionCommands(csd.getConstraintStatus())) {
        result.add(new EMFCommandMarkerResolution(TransactionUtil.getEditingDomain(csd.getConstraintStatus().getTarget()), c));
      }
    }
    return result.toArray(new IMarkerResolution[result.size()]);
  }

}