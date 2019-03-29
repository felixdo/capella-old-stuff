package org.polarsys.capella.core.data.cs.validation2.tests;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.polarsys.capella.core.data.cs.validation2.FE_I_ExchangeItemConsistency.NO_UNUSED_EI_ON_FE_ID;
import static org.polarsys.capella.core.data.cs.validation2.FE_I_ExchangeItemConsistency.NO_UNUSED_EI_ON_FE_WITH_ALLOCATIONS_ID;
import static org.polarsys.capella.core.data.cs.validation2.I_FE_ExchangeItemConsistency.NO_UNUSED_EI_ON_I_ID;
import static org.polarsys.capella.core.data.cs.validation2.I_FE_ExchangeItemConsistency.NO_UNUSED_EI_ON_I_WITH_ALLOCATIONS;
import static org.polarsys.capella.core.data.cs.validation2.tests.common.StatusMatcher.okStatus;
import static org.polarsys.capella.core.data.cs.validation2.helpers.Allocators.allocate;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.cs.validation2.ExchangeExtent;
import org.polarsys.capella.core.data.cs.validation2.tests.common.ValidationHelpers;
import org.polarsys.capella.core.data.fa.ComponentFunctionalAllocation;
import org.polarsys.capella.core.data.fa.ComponentPort;
import org.polarsys.capella.core.data.fa.FunctionalExchange;
import org.polarsys.capella.core.model.helpers.FunctionalExchangeExt;

/**
 * Test validation of a potential interface exchange.
 * 
 * It's unclear what the validation result should be here.. at least check that 
 * there are no exceptions..
 */
public class FE_I_With_PotentialInterface_Exchange extends IFEAllocationTestTemplate {

  FunctionalExchange fe;

  @Override
  public void test() throws Exception {

    ComponentPort providerCp = left.cp1;
    ComponentPort requirerCp = center.cp1;

      executeCommand(() -> {
        providerCp.getProvidedInterfaces().add(i2);
        requirerCp.getRequiredInterfaces().add(i2);
        fe = FunctionalExchangeExt.createFunctionalExchange(center.fop1_1, left.fip1_1);
        allocate(ei1).on(fe);
        ComponentFunctionalAllocation alloc = left.pf1.getComponentFunctionalAllocations().get(0);
        EcoreUtil.delete(alloc);
      });

      assertThat(unusedEIOnFunctionalExchanges(center.component, false), is(okStatus()));
      assertThat(unusedEIOnFunctionalExchanges(center.component, true), is(okStatus()));

      assertSame(ExchangeExtent.POTENTIAL_INTERFACE, ExchangeExtent.of(fe, center.component));
      assertSame(ExchangeExtent.EXTERNAL, ExchangeExtent.of(fe, left.component));
      
      executeCommand(() -> {
        allocate(center.fop1_1).on(requirerCp);
        allocate(center.fip2_1).on(providerCp);
      });

      assertThat(unusedEIOnFunctionalExchanges(center.component, false), is(okStatus()));
      assertThat(unusedEIOnFunctionalExchanges(center.component, true), is(okStatus()));    

  }

  protected IStatus unusedEIOnInterfaces(Component c, boolean considerAllocations){
    return ValidationHelpers.validate(c, considerAllocations ? NO_UNUSED_EI_ON_I_WITH_ALLOCATIONS : NO_UNUSED_EI_ON_I_ID);
  }

  protected IStatus unusedEIOnFunctionalExchanges(Component c, boolean considerAllocations){
    return ValidationHelpers.validate(c, considerAllocations ? NO_UNUSED_EI_ON_FE_WITH_ALLOCATIONS_ID : NO_UNUSED_EI_ON_FE_ID);
  }

}
