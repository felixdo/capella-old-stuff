package org.polarsys.capella.core.data.cs.validation2.accel;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CommandWrapper;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.polarsys.capella.common.helpers.EObjectLabelProviderHelper;
import org.polarsys.capella.common.ui.toolkit.dialogs.SelectElementsDialog;
import org.polarsys.capella.core.data.cs.validation2.Activator;
import org.polarsys.capella.core.data.cs.validation2.ModelHelpers;
import org.polarsys.capella.core.data.fa.FaPackage;
import org.polarsys.capella.core.data.fa.FunctionalExchange;
import org.polarsys.capella.core.data.information.ExchangeItem;

public class PropagateFxP2FEHandler extends AbstractHandler {
  
  public static final String PARAMETER_MODE = "parameterMode"; //$NON-NLS-1$
  public static final String MODE_UNION = "union"; //$NON-NLS-1$
  public static final String MODE_INTERSECTION = "intersection"; //$NON-NLS-1$
  public static final String MODE_SELECT = "select"; //$NON-NLS-1$
  
  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    List<?> selection = HandlerUtil.getCurrentStructuredSelection(event).toList();

    Map<TransactionalEditingDomain, List<FunctionalExchange>> fes = selection.stream()
        .filter(FunctionalExchange.class::isInstance)
        .map(FunctionalExchange.class::cast)
        .collect(Collectors.groupingBy(TransactionUtil::getEditingDomain));

    String mode = event.getParameter("mode"); //$NON-NLS-1$
    EReference ref = FaPackage.Literals.FUNCTIONAL_EXCHANGE__EXCHANGED_ITEMS;
    
    for (final TransactionalEditingDomain domain : fes.keySet()) {
      Command cmd = fes.get(domain).stream().collect(ModelHelpers.createCommandCollector(
          // don't select any other object after completion..
          (FunctionalExchange fe) ->
            new CommandWrapper(AddCommand.create(domain, fe, ref, getModeFunction(event, mode).apply(fe))) {
              public Collection<?> getAffectedObjects(){
                return Collections.emptyList();
              }
            }
          ));
      domain.getCommandStack().execute(cmd);
    }
    return null;
  }

  private Function<FunctionalExchange, Collection<ExchangeItem>> getModeFunction(ExecutionEvent event, String mode) {
    Function<FunctionalExchange, Collection<ExchangeItem>> func = null;
    if (MODE_UNION.equals(mode)) {
      func = this::handleUnion;
    } else if (MODE_INTERSECTION.equals(mode)) {
      func = this::handleIntersection;
    } else if (MODE_SELECT.equals(mode)) {
      func = fe -> handleSelection(fe, HandlerUtil.getActiveShell(event));
    } else {
      throw new IllegalArgumentException("Unknown mode parameter"); //$NON-NLS-1$
    }
    return func;
  }

  private Collection<ExchangeItem> handleIntersection(FunctionalExchange fe) {
    Collection<ExchangeItem> ei = new HashSet<ExchangeItem>();
    if (fe.getSourceFunctionOutputPort() != null) {
      ei.addAll(fe.getSourceFunctionOutputPort().getOutgoingExchangeItems());
    }
    if (fe.getTargetFunctionInputPort() != null) {
      ei.retainAll(new HashSet<>(fe.getTargetFunctionInputPort().getIncomingExchangeItems()));
    }
    ei.removeAll(fe.getExchangedItems());
    return ei;
  }

  private Collection<ExchangeItem> handleSelection(FunctionalExchange fes, Shell shell) {
    Collection<ExchangeItem> ei = handleUnion(fes);
    TransactionalEditingDomain domain = TransactionUtil.getEditingDomain(fes);
    
    SelectElementsDialog dialog = new SelectElementsDialog(shell, domain, ((AdapterFactoryEditingDomain) domain).getAdapterFactory(), 
        Activator.getResourceString("%propagateFxP2FE_dialogTitle"), //$NON-NLS-1$
        NLS.bind(Activator.getResourceString("%propagateFxP2FE_dialogMessage"), EObjectLabelProviderHelper.getText(fes)), //$NON-NLS-1$
        ei);
    if (dialog.open() == Window.OK) {
      return (Collection<ExchangeItem>) dialog.getResult();
    } else {
      return Collections.emptyList();
    }
  }

  private Collection<ExchangeItem> handleUnion(FunctionalExchange fe) {
    Collection<ExchangeItem> ei = new HashSet<ExchangeItem>();
    if (fe.getSourceFunctionOutputPort() != null) {
      ei.addAll(fe.getSourceFunctionOutputPort().getOutgoingExchangeItems());
    }
    if (fe.getTargetFunctionInputPort() != null) {
      ei.addAll(new HashSet<>(fe.getTargetFunctionInputPort().getIncomingExchangeItems()));
    }
    ei.removeAll(fe.getExchangedItems());
    return ei;
  }

}
