package org.polarsys.capella.core.data.cs.validation2;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.validation.IValidationContext;
import org.eclipse.emf.validation.model.ConstraintStatus;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.cs.CsPackage;
import org.polarsys.capella.core.data.cs.PhysicalPort;
import org.polarsys.capella.core.data.fa.ComponentPort;
import org.polarsys.capella.core.data.fa.FaPackage;
import org.polarsys.capella.core.data.fa.FunctionInputPort;
import org.polarsys.capella.core.data.fa.FunctionOutputPort;
import org.polarsys.capella.core.validation.rule.AbstractValidationRule;

public class ExchangePortNameConsistency extends AbstractValidationRule {

  public static final String ID_FXP_FE = "org.polarsys.capella.core.data.cs.validation2.fxp_fe_name"; //$NON-NLS-1$
  public static final String ID_CP_CE = "org.polarsys.capella.core.data.cs.validation2.cp_ce_name"; //$NON-NLS-1$
  public static final String ID_PP_PL = "org.polarsys.capella.core.data.cs.validation2.pp_pl_name"; //$NON-NLS-1$
  
  private final ValidationUtil util = new ValidationUtil(Activator.getContext().getBundle());
  
  static final Map<EClass, Function<EObject, List<? extends NamedElement>>> map;
  
  static {
    map = new HashMap<>();
    map.put(FaPackage.Literals.FUNCTION_OUTPUT_PORT, e -> ((FunctionOutputPort) e).getOutgoingFunctionalExchanges());
    map.put(FaPackage.Literals.FUNCTION_INPUT_PORT, e -> ((FunctionInputPort) e).getIncomingFunctionalExchanges());
    map.put(FaPackage.Literals.COMPONENT_PORT, e -> ((ComponentPort) e).getComponentExchanges());
    map.put(CsPackage.Literals.PHYSICAL_PORT,  e -> ((PhysicalPort) e).getInvolvedLinks());
  }

  @Override
  public IStatus validate(IValidationContext ctx) {
    Function<EObject, List<? extends NamedElement>> func = map.get(ctx.getTarget().eClass());
    NamedElement reference = unique(func.apply(ctx.getTarget()));
    NamedElement current = (NamedElement) ctx.getTarget();
    
    if (reference != null && !Objects.equals(reference.getName(), current.getName())) {
      return ConstraintStatus.createStatus(ctx, ctx.getTarget(), Collections.singleton(reference), IStatus.ERROR, 0, 
          util.getMessageTemplate(ctx),
          current.getName() == null ? "" : current.getName(),  //$NON-NLS-1$
          reference.getName() == null? "" : reference.getName()); //$NON-NLS-1$
    }
    return ctx.createSuccessStatus();
  }
  
  static NamedElement unique(List<? extends NamedElement> candidates) {
    return candidates.size() == 1 ? candidates.get(0) : null;
  }

  
}
