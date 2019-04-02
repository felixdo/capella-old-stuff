package org.polarsys.capella.core.data.cs.validation2.accel.common;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.ui.handlers.HandlerUtil;
import org.polarsys.capella.core.data.cs.validation2.ModelHelpers;

public abstract class DomainHandler<T> extends AbstractHandler {

  final Class<T> domainClass;

  protected DomainHandler(Class<T> domainClass) {
    this.domainClass = domainClass;
  }

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    Stream<?> selection = HandlerUtil.getCurrentStructuredSelection(event).toList().stream();
    Stream<T> adaptedSelection = ModelHelpers.adapt(selection).filter(domainClass::isInstance).map(domainClass::cast);
    Map<TransactionalEditingDomain, List<T>> byEditingDomain = adaptedSelection.collect(Collectors.groupingBy(TransactionUtil::getEditingDomain));
    for (TransactionalEditingDomain domain : byEditingDomain.keySet()) {
      domain.getCommandStack().execute(createCommand(event, domain, byEditingDomain.get(domain).stream()));
    }
    return null;
  }

  protected abstract Command createCommand(ExecutionEvent event, TransactionalEditingDomain domain, Stream<T> list);

  protected final Class<T> getDomainClass(){
    return domainClass;
  }

}
