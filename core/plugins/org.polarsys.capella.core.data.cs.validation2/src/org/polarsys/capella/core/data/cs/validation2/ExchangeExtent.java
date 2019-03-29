package org.polarsys.capella.core.data.cs.validation2;

import java.util.Collection;
import java.util.function.Predicate;

import org.polarsys.capella.common.data.activity.ActivityNode;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.fa.AbstractFunction;
import org.polarsys.capella.core.data.fa.AbstractFunctionalBlock;
import org.polarsys.capella.core.data.fa.FunctionalExchange;

/**
 * The extent of an exchange in the context of a given component.
 */
public enum ExchangeExtent {

  /**
   * The exchange is completely internal to its context component
   */
  INTERNAL,

  /**
   * The exchange is interfacing, meaning that one side is inside, and the other end
   * is not inside the context component
   */
  INTERFACE,
  
  /**
   * The exchange is potentially interfacing, meaning that one side is inside, and the other 
   * end is unknown/not allocated
   */
  POTENTIAL_INTERFACE,
  
  /**
   * The extent that is used if no other extent definition matches the exchange, e.g. an exchange without
   * source/target ports, or one whose source/target functions are not allocated anywhere, or one that
   * is completely outside of the context component.
   */
  EXTERNAL;
  
  /**
   * Calculates the the extent for an exchange in a given context
   * 
   * @param fe a FunctionalExchange 
   * @param context the context component
   * @return
   */
  public static ExchangeExtent of(FunctionalExchange fe, Component context) {

    ExchangeExtent result = EXTERNAL;

    ActivityNode source = fe.getSource();
    ActivityNode target = fe.getTarget();
    
    if (source != null && target != null && source.eContainer() instanceof AbstractFunction && target.eContainer() instanceof AbstractFunction) {

      AbstractFunction outF = (AbstractFunction) source.eContainer();
      AbstractFunction inF = (AbstractFunction) target.eContainer();

      AbstractFunctionalBlock outAllocator = outF.getAllocationBlocks().isEmpty() ? null : outF.getAllocationBlocks().get(0);
      AbstractFunctionalBlock inAllocator = inF.getAllocationBlocks().isEmpty() ? null : inF.getAllocationBlocks().get(0);
      
      Collection<Component> hierarchy = ModelHelpers.getComponentHierarchy(context);
      
      if (hierarchy.contains(outAllocator) && hierarchy.contains(inAllocator)) {
        result = INTERNAL;
      } else if (hierarchy.contains(outAllocator)) {
        if (inAllocator == null) {
          result = ExchangeExtent.POTENTIAL_INTERFACE;
        } else {
          result = ExchangeExtent.INTERFACE;
        }
      } else if (hierarchy.contains(inAllocator)) {
        if (outAllocator == null) {
          result = ExchangeExtent.POTENTIAL_INTERFACE;
        } else {
          result = ExchangeExtent.INTERFACE;
        }
      }
    }

    return result;
  }

  public static Predicate<FunctionalExchange> match(ExchangeExtent e, Component c){
    return new Predicate<FunctionalExchange>() {
      @Override
      public boolean test(FunctionalExchange t) {
        return e == of(t,c);
      }
    };
  }

}