package org.polarsys.capella.core.data.cs.validation2.tests;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.polarsys.capella.core.data.cs.validation2.FE_I_ExchangeItemConsistency.NO_UNUSED_EI_ON_FE_ID;
import static org.polarsys.capella.core.data.cs.validation2.FE_I_ExchangeItemConsistency.NO_UNUSED_EI_ON_FE_WITH_ALLOCATIONS_ID;
import static org.polarsys.capella.core.data.cs.validation2.I_FE_ExchangeItemConsistency.NO_UNUSED_EI_ON_I_ID;
import static org.polarsys.capella.core.data.cs.validation2.I_FE_ExchangeItemConsistency.NO_UNUSED_EI_ON_I_WITH_ALLOCATIONS;
import static org.polarsys.capella.core.data.cs.validation2.tests.common.StatusMatcher.okStatus;
import static  org.polarsys.capella.core.data.cs.validation2.helpers.Allocators.allocate;

import org.eclipse.core.runtime.IStatus;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.cs.validation2.tests.common.ValidationHelpers;
import org.polarsys.capella.core.data.fa.ComponentPort;
import org.polarsys.capella.core.data.fa.FunctionInputPort;
import org.polarsys.capella.core.data.fa.FunctionOutputPort;
import org.polarsys.capella.core.data.fa.FunctionalExchange;
import org.polarsys.capella.core.data.pa.PhysicalComponent;
import org.polarsys.capella.core.data.pa.PhysicalFunction;
import org.polarsys.capella.core.model.skeleton.CapellaModelSkeleton;

/**
 * Test that ExchangeItem on Interface between parent components is justified if there's an FE between sub-components
 * that uses the Exchange Item
 */
public class IFEAllocationTest4 extends IFEAllocationTestTemplate {

  PhysicalComponent centerSub;
  PhysicalComponent leftSub;
  
  PhysicalFunction centerSubPF;
  PhysicalFunction leftSubPF;
  
  FunctionalExchange subcomponentFE;

  FunctionOutputPort leftFoP;
  FunctionInputPort centerFiP;
  
  public void test() throws Exception {
    
    assertThat(unusedEIOnInterfaces(center.component, false), is(okStatus()));
    assertThat(unusedEIOnInterfaces(center.component, true), is(okStatus()));
    assertThat(unusedEIOnInterfaces(left.component, false), is(okStatus()));
    assertThat(unusedEIOnInterfaces(left.component, true), is(okStatus()));


    ComponentPort requirerCp = left.cp1;
    ComponentPort providerCp = center.cp1;

    executeCommand(()->{
      providerCp.getProvidedInterfaces().add(i1);
      requirerCp.getRequiredInterfaces().add(i1);
    });

    assertThat(unusedEIOnInterfaces(center.component, false), is(not(okStatus())));
    assertThat(unusedEIOnInterfaces(center.component, true), is(not(okStatus())));
    assertThat(unusedEIOnInterfaces(left.component, false), is(not(okStatus())));
    assertThat(unusedEIOnInterfaces(left.component, true), is(not(okStatus())));

    // now allocate the missing ei on the sub-fe
    executeCommand(() -> allocate(ei1, ei2).on(subcomponentFE));
    
    // this satisfies the rule that doesn't care about fxp-cp allocations
    assertThat(unusedEIOnInterfaces(center.component, false), is(okStatus()));
    assertThat(unusedEIOnInterfaces(center.component, true), is(not(okStatus())));
    assertThat(unusedEIOnInterfaces(left.component, false), is(okStatus()));
    assertThat(unusedEIOnInterfaces(left.component, true), is(not(okStatus())));

    // now allocate the function ports to their parent component cps
    executeCommand(()->{
      allocate(subcomponentFE.getSourceFunctionOutputPort()).on(requirerCp);
      allocate(subcomponentFE.getTargetFunctionInputPort()).on(providerCp);
    });

    // should satisfy all variations
    assertThat(unusedEIOnInterfaces(center.component, false), is(okStatus()));
    assertThat(unusedEIOnInterfaces(center.component, true), is(okStatus()));
    assertThat(unusedEIOnInterfaces(left.component, false), is(okStatus()));
    assertThat(unusedEIOnInterfaces(left.component, true), is(okStatus()));

  }

  @Override
  protected void initModel(CapellaModelSkeleton skeleton) {
    super.initModel(skeleton);
    
    centerSub = create(center.component, PC);
    centerSub.setName("centerSub"); //$NON-NLS-1$
    leftSub = create(left.component, PC);
    leftSub.setName("leftSub"); //$NON-NLS-1$
    
    centerSubPF = create(center.pf1.eContainer(), PF);
    leftSubPF = create(left.pf1.eContainer(), PF);

    leftFoP = create(leftSubPF, FOP);
    centerFiP = create(centerSubPF, FIP);

    allocate(leftSubPF).on(leftSub);
    allocate(centerSubPF).on(centerSub);

    subcomponentFE = create(leftSubPF.eContainer(), FE);
    subcomponentFE.setSource(leftFoP);
    
    subcomponentFE.setTarget(centerFiP);

  }

  protected IStatus unusedEIOnInterfaces(Component c, boolean considerAllocations){
    return ValidationHelpers.validate(c, considerAllocations ? NO_UNUSED_EI_ON_I_WITH_ALLOCATIONS : NO_UNUSED_EI_ON_I_ID);
  }

  protected IStatus unusedEIOnFunctionalExchanges(Component c, boolean considerAllocations){
    return ValidationHelpers.validate(c, considerAllocations ? NO_UNUSED_EI_ON_FE_WITH_ALLOCATIONS_ID : NO_UNUSED_EI_ON_FE_ID);
  }

}
