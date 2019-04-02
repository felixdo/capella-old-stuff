package org.polarsys.capella.core.data.cs.validation2.accel;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CommandWrapper;

class HideAffectedObjectsCommandWrapper extends CommandWrapper {

  public HideAffectedObjectsCommandWrapper(Command command) {
    super(command);
  }

  public HideAffectedObjectsCommandWrapper(String label, String description, Command command) {
    super(label, description, command);
  }

  @Override
  public Collection<?> getAffectedObjects() {
    return Collections.emptyList();
  }

  public static Command hide(Command c) {
    return new HideAffectedObjectsCommandWrapper(c);
  }
  
}
