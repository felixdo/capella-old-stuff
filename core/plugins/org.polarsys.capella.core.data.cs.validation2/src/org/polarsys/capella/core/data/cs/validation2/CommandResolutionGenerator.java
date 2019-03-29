package org.polarsys.capella.core.data.cs.validation2;

import java.util.Collection;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.validation.model.IConstraintStatus;

public interface CommandResolutionGenerator {

  Collection<? extends Command> generateResolutionCommands(IConstraintStatus constraintStatus);

}
