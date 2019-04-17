package org.polarsys.capella.core.data.cs.validation2;

import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.osgi.util.NLS;
import org.polarsys.capella.common.helpers.EObjectLabelProviderHelper;
import org.polarsys.capella.core.data.information.ExchangeItem;

public abstract class AbstractExchangeItemCommand extends RecordingCommand {

  private final Collection<? extends EObject> targets;
  private final ExchangeItem exchangeItem;

  public AbstractExchangeItemCommand(ExchangeItem ei, Collection<? extends EObject> targets, String labelKey) {
    super(TransactionUtil.getEditingDomain(ei));
    this.targets = targets;
    this.exchangeItem = ei;
    setLabel(NLS.bind(Activator.getResourceString(labelKey), EObjectLabelProviderHelper.getText(ei),
        targets.stream().map(EObjectLabelProviderHelper::getText).collect(Collectors.joining(", "))));  //$NON-NLS-1$
  }
  
  protected final Collection<? extends EObject> getTargets(){
    return targets;
  }
  
  protected final ExchangeItem getExchangeItem() {
    return exchangeItem;
  }
}
