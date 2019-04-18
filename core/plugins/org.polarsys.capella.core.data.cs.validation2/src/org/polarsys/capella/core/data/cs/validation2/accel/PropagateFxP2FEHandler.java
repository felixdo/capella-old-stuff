package org.polarsys.capella.core.data.cs.validation2.accel;

import static org.polarsys.capella.core.data.cs.validation2.ModelHelpers.createCommandCollector;
import static org.polarsys.capella.core.data.cs.validation2.accel.HideAffectedObjectsCommandWrapper.hide;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.emf.common.command.Command;
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
import org.polarsys.capella.core.data.cs.validation2.accel.common.DomainHandler;
import org.polarsys.capella.core.data.fa.FaPackage;
import org.polarsys.capella.core.data.fa.FunctionalExchange;
import org.polarsys.capella.core.data.information.ExchangeItem;

public class PropagateFxP2FEHandler extends DomainHandler<FunctionalExchange> {
  
  public static final String PARAMETER_MODE = "mode"; //$NON-NLS-1$
  public static final String MODE_UNION = "union"; //$NON-NLS-1$
  public static final String MODE_INTERSECTION = "intersection"; //$NON-NLS-1$
  public static final String MODE_SELECT = "select"; //$NON-NLS-1$
  private final EReference ref = FaPackage.Literals.FUNCTIONAL_EXCHANGE__EXCHANGED_ITEMS;

  public PropagateFxP2FEHandler() {
    super(FunctionalExchange.class);
  }


  @Override
  protected Command createCommand(ExecutionEvent event, TransactionalEditingDomain domain,
      Stream<FunctionalExchange> list) {
    return list.collect(createCommandCollector(
        (FunctionalExchange fe) ->
          hide(AddCommand.create(domain, fe, ref, getModeFunction(event).apply(fe))) 
        ));
  }

  private Function<FunctionalExchange, Collection<ExchangeItem>> getModeFunction(ExecutionEvent event) {
    String mode = event.getParameter(PARAMETER_MODE);
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

  @SuppressWarnings("unchecked")
  private Collection<ExchangeItem> handleSelection(FunctionalExchange fes, Shell shell) {
    Collection<ExchangeItem> ei = handleUnion(fes);
    TransactionalEditingDomain domain = TransactionUtil.getEditingDomain(fes);
    
    SelectElementsDialog dialog = new SelectElementsDialog(shell, domain, ((AdapterFactoryEditingDomain) domain).getAdapterFactory(), 
        Activator.getResourceString("%dialogTitle_pullFxPEI2FE_select"), //$NON-NLS-1$
        NLS.bind(Activator.getResourceString("%dialogMessage_pullFxPEI2FE_select"), EObjectLabelProviderHelper.getText(fes)), //$NON-NLS-1$
        ei, true, null);
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
