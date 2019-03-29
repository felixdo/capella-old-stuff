package org.polarsys.capella.core.data.cs.validation2.tests;

import static  org.polarsys.capella.core.data.cs.validation2.helpers.Allocators.allocate;

import org.polarsys.capella.core.data.cs.Interface;
import org.polarsys.capella.core.data.cs.InterfacePkg;
import org.polarsys.capella.core.data.cs.validation2.tests.common.ComponentTemplate1;
import org.polarsys.capella.core.data.cs.validation2.tests.common.DynamicValidationTest;
import org.polarsys.capella.core.data.information.DataPkg;
import org.polarsys.capella.core.data.information.ExchangeItem;
import org.polarsys.capella.core.model.skeleton.CapellaModelSkeleton;

public abstract class IFEAllocationTestTemplate extends DynamicValidationTest {

  protected ComponentTemplate1 left;
  protected ComponentTemplate1 center;
  protected ComponentTemplate1 right;

  protected ExchangeItem ei1;
  protected ExchangeItem ei2;
  protected ExchangeItem ei3;
  protected ExchangeItem ei4;
  protected ExchangeItem ei5;
  protected ExchangeItem ei6;
  
  /**
   * An initially unused interface with ei1 and ei2 allocated to it
   */
  protected Interface i1;

  protected Interface i2;
  protected Interface i3;
  
  /**
   * Initialize the model by creating model elements etc. Called during setup in a write transaction.
   * 
   * @param skeleton the model skeleton
   */
  protected void initModel(CapellaModelSkeleton skeleton) {
    left = new ComponentTemplate1(skeleton, this);
    left.component.setName("left"); //$NON-NLS-1$

    center = new ComponentTemplate1(skeleton, this);
    center.component.setName("center"); //$NON-NLS-1$

    right = new ComponentTemplate1(skeleton, this);
    right.component.setName("right"); //$NON-NLS-1$
    
    DataPkg data = skeleton.getPhysicalArchitecture().getOwnedDataPkg();
    ei1 = create(data, EI);
    ei2 = create(data, EI);
    ei3 = create(data, EI);
    ei4 = create(data, EI);
    ei5 = create(data, EI);
    ei6 = create(data, EI);
    
    InterfacePkg ifpkg = skeleton.getPhysicalArchitecture().getOwnedInterfacePkg();
    i1 = create(ifpkg, I);
    i2 = create(ifpkg, I);
    i3 = create(ifpkg, I);
    
    allocate(ei1).on(i1);
    allocate(ei2).on(i1);

  }

}
