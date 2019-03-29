package org.polarsys.capella.core.data.cs.validation2;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.polarsys.capella.common.data.activity.InputPin;
import org.polarsys.capella.common.data.activity.OutputPin;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.cs.Part;
import org.polarsys.capella.core.data.fa.AbstractFunction;
import org.polarsys.capella.core.data.fa.AbstractFunctionalBlock;
import org.polarsys.capella.core.data.fa.ComponentPort;
import org.polarsys.capella.core.data.fa.FunctionInputPort;
import org.polarsys.capella.core.data.fa.FunctionOutputPort;
import org.polarsys.capella.core.data.fa.FunctionPort;
import org.polarsys.capella.core.data.fa.FunctionalExchange;
import org.polarsys.capella.core.model.helpers.ComponentExt;

public class ModelHelpers {
  
  public static void splitPorts(Collection<FunctionPort> in, Collection<FunctionInputPort> fips, Collection<FunctionOutputPort> fops) {
    for (FunctionPort fp : in) {
      if (fp instanceof FunctionOutputPort && fops != null) {
        fops.add((FunctionOutputPort) fp);
      } else if (fp instanceof FunctionInputPort && fips != null) {
        fips.add((FunctionInputPort) fp);
      }
    }
  }

  public static Collection<FunctionOutputPort> getFunctionOutputPorts(AbstractFunction func){
    return getFopStream(func).collect(Collectors.toList());
  }
  
  public static Collection<FunctionInputPort> getFunctionInputPorts(AbstractFunction func){
    return getFipStream(func).collect(Collectors.toList());
  }

  private static Stream<FunctionInputPort> getFipStream(AbstractFunction func){
    return func.getInputs().stream().filter(FunctionInputPort.class::isInstance).map(FunctionInputPort.class::cast);
  }
  
  private static Stream<FunctionOutputPort> getFopStream(AbstractFunction func){
    return func.getOutputs().stream().filter(FunctionOutputPort.class::isInstance).map(FunctionOutputPort.class::cast);
  }
  
  public static Collection<FunctionalExchange> getIncomingInterfaceExchanges(FunctionInputPort port, Component component){
    return port.getIncomingFunctionalExchanges().stream().filter(fe->ExchangeExtent.of(fe, component) == ExchangeExtent.INTERFACE).collect(Collectors.toList()); 
  }

  public static Collection<FunctionalExchange> getOutgoingInterfaceExchanges(FunctionOutputPort port, Component component){
    return port.getOutgoingFunctionalExchanges().stream().filter(fe->ExchangeExtent.of(fe, component) == ExchangeExtent.INTERFACE).collect(Collectors.toList());
  }

  private static <FxP extends FunctionPort> Predicate<FxP> isInterfaceFxP(Component component, Function<FxP, Collection<FunctionalExchange>> getFunctionalExchanges){
    return new Predicate<FxP>() {
      @Override
      public boolean test(FxP t) {
        Collection<FunctionalExchange> fes = getFunctionalExchanges.apply(t);
        if (fes.isEmpty()) {
          return false;
        }
        for (FunctionalExchange fe : fes) {
          switch(ExchangeExtent.of(fe, component)) {
            case INTERFACE: break;
            default:return false;
          }
        }
        return true;
      }
    };
  }

  // the external fips are these who only connect to external exchanges
  public static Function<AbstractFunction, Collection<FunctionInputPort>> getExternalFunctionInputPorts(Component component){
    return new Function<AbstractFunction, Collection<FunctionInputPort>>(){
      @Override
      public Collection<FunctionInputPort> apply(AbstractFunction t) {
        return getFipStream(t).filter(isInterfaceFxP(component, FunctionInputPort::getIncomingFunctionalExchanges)).collect(Collectors.toList());
      }
    };
  }

  // the external fops are those who only connect to external exchanges
  public static Function<AbstractFunction, Collection<FunctionOutputPort>> getExternalFunctionOutputPorts(Component component){
    return new Function<AbstractFunction, Collection<FunctionOutputPort>>(){
      @Override
      public Collection<FunctionOutputPort> apply(AbstractFunction t) {
        return getFopStream(t).filter(isInterfaceFxP(component, FunctionOutputPort::getOutgoingFunctionalExchanges)).collect(Collectors.toList());
      }
    };
  }

  public static Collection<FunctionInputPort> getAllocatedFunctionInputPorts(ComponentPort port){
    Collection<FunctionInputPort> result = new ArrayList<>();
    splitPorts(port.getAllocatedFunctionPorts(), result, null);
    return result;
  }

  public static Collection<FunctionOutputPort> getAllocatedFunctionOutputPorts(ComponentPort port){
    Collection<FunctionOutputPort> result = new ArrayList<>();
    splitPorts(port.getAllocatedFunctionPorts(), null, result);
    return result;
  }
  
  public static Stream<FunctionOutputPort> getAllFunctionOutputPorts(Component c){
    Collection<FunctionOutputPort> result = new ArrayList<>();
    for (AbstractFunction f : c.getAllocatedFunctions()) {
      for (OutputPin pin : f.getOutputs()) {
        if (pin instanceof FunctionOutputPort) {
          result.add((FunctionOutputPort) pin);
        }
      }
    }
    return result.stream();
  }

  public static Stream<FunctionInputPort> getAllFunctionInputPorts(Component c){
    Collection<FunctionInputPort> result = new ArrayList<>();
    for (AbstractFunction f : c.getAllocatedFunctions()) {
      for (InputPin pin : f.getInputs()) {
        if (pin instanceof FunctionInputPort) {
          result.add((FunctionInputPort) pin);
        }
      }
    }
    return result.stream();
  }
  
  public static Stream<FunctionInputPort> getAllHierarchyFunctionInputPorts(Component root){
    return getComponentHierarchy(root).stream().flatMap(ModelHelpers::getAllFunctionInputPorts);
  }
  
  public static Stream<FunctionOutputPort> getAllHierarchyFunctionOutputPorts(Component root){
    return getComponentHierarchy(root).stream().flatMap(ModelHelpers::getAllFunctionOutputPorts);
  }
 
  public static Stream<AbstractFunction> getAllHierarchyAllocatedFunctions(Component root){
    return getComponentHierarchy(root).stream().flatMap(c -> c.getAllocatedFunctions().stream());
  }

  /**
   * Returns the hierarchy of a given component. This hierarchy is not defined
   * by the components that are contained in the component, but by the types of 
   * its nested partitions.
   * <p> What makes this confusing is the fact that capella creates components
   * and partitions side by side, so the component containment relation is
   * seemingly identical to the component hierarchy.</p> 
   * </p>
   * The result is a breath first order, including the root element.
   * Each component in the hierarchy is listed only once and cyclic definitions
   * are ignored.
   * @param root
   * @return
   */
  public static Collection<Component> getComponentHierarchy(Component root){
    // TODO this can be done lazily somehow...
    Set<Component> result = new LinkedHashSet<>();
    Deque<Component> toVisit = new ArrayDeque<Component>();
    toVisit.add(root);    
    while (!toVisit.isEmpty()) {
      Component current = toVisit.pop();
      if (result.add(current)) {        
        for (Part p : ComponentExt.getSubParts(current)) {
          if (p.getType() instanceof Component) {
            toVisit.add((Component) p.getType());
          }
        }
      }
    }
    return result;
  }

  
  /**
   * The default allocator component for a Function Port is the component that allocates the ports owning function,
   * or null if the port has no owning function or if that function is not allocated to any component.
   * @param fp
   * @return
   */
  public static Component getDefaultAllocatorComponent(FunctionPort fp) {
    if (fp.eContainer() instanceof AbstractFunction) {
      AbstractFunction af = (AbstractFunction) fp.eContainer();
      for (AbstractFunctionalBlock ab : af.getAllocationBlocks()) {
        if (ab instanceof Component) {
          return (Component) ab;
        }
      }
    }
    return null;
  }
  
  private static Collection<ComponentPort> getAllocatorComponentPortCandidates(FunctionPort fp){
    Collection<ComponentPort> result = new ArrayList<ComponentPort>();
    AbstractFunction af = (AbstractFunction) fp.eContainer();
    for (AbstractFunctionalBlock afb : af.getAllocationBlocks()) {
      if (afb instanceof Component) {
        result.addAll(((Component) afb).getContainedComponentPorts());
      }
    }
    return result;
  }
  
  /**
   * If the given fp has one or more allocator component ports, returns a collection
   * containing these allocator ports, otherwise returns all component ports of
   * the component that allocates the function.
   * @param fp
   * @return
   */
  public static Collection<ComponentPort> getAllocatorComponentPortsWithFallback(FunctionPort fp){
    if (fp.getAllocatorComponentPorts().size() > 0) {
      return fp.getAllocatorComponentPorts();
    }
    return getAllocatorComponentPortCandidates(fp);
  }

}
