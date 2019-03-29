package org.polarsys.capella.core.data.cs.validation2;

import java.util.Collections;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.transaction.RollbackException;
import org.eclipse.emf.transaction.TransactionalCommandStack;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.polarsys.capella.core.validation.ui.ide.PluginActivator;
import org.polarsys.capella.core.validation.ui.ide.quickfix.AbstractCapellaMarkerResolution;

public class EMFCommandMarkerResolution extends AbstractCapellaMarkerResolution {

  private final TransactionalEditingDomain domain;
  private final Command command;

  public EMFCommandMarkerResolution(TransactionalEditingDomain domain, Command command) {
    this.domain = domain;
    this.command = command;
    setLabel(command.getLabel());
    setDescription(command.getDescription());
  }

  @Override
  public void run(IMarker marker) {
    if (marker.exists() && command.canExecute()){
      try {
        ((TransactionalCommandStack)domain.getCommandStack()).execute(command, Collections.emptyMap());
        try {
          marker.delete();
        } catch (CoreException e){
          PluginActivator.getDefault().log(IStatus.ERROR, e.getLocalizedMessage(), e);
        }
      } catch (InterruptedException | RollbackException e) {
        PluginActivator.getDefault().log(IStatus.ERROR, e.getLocalizedMessage(), e);
      }
    }
  }
  
}