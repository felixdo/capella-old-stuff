package org.polarsys.capella.core.data.cs.validation2.tests;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;
import static org.polarsys.capella.core.data.cs.validation2.FE_I_ExchangeItemConsistency.NO_UNUSED_EI_ON_FE_ID;
import static org.polarsys.capella.core.data.cs.validation2.FE_I_ExchangeItemConsistency.NO_UNUSED_EI_ON_FE_WITH_ALLOCATIONS_ID;
import static org.polarsys.capella.core.data.cs.validation2.I_FE_ExchangeItemConsistency.NO_UNUSED_EI_ON_I_ID;
import static org.polarsys.capella.core.data.cs.validation2.I_FE_ExchangeItemConsistency.NO_UNUSED_EI_ON_I_WITH_ALLOCATIONS;
import static org.polarsys.capella.core.data.cs.validation2.tests.common.StatusMatcher.okStatus;
import static org.polarsys.capella.core.model.helpers.Allocators.allocate;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.validation.model.IConstraintStatus;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.cs.validation2.ExchangeItemAllocationResolver;
import org.polarsys.capella.core.data.cs.validation2.tests.common.ValidationHelpers;
import org.polarsys.capella.core.data.fa.ComponentPort;
import org.polarsys.capella.core.data.fa.FunctionalExchange;

/**
 * Tests the variations of ExchangeItem allocation consistency between Interfaces and Functional Exchanges and vice versa.
 */
public class IFEAllocationTest1 extends IFEAllocationTestTemplate {
  
  protected FunctionalExchange fe1_center_left;
  protected FunctionalExchange fe2_center_left;
  protected FunctionalExchange fe3_center_right;
  
  protected ExchangeItemAllocationResolver resolver = new ExchangeItemAllocationResolver(false);
  
  @Override
  public void test() throws Exception {

    ComponentPort providerCp = left.cp1;
    ComponentPort requirerCp = center.cp1;
    
    executeCommand(() -> {
      providerCp.getProvidedInterfaces().add(i1);
      requirerCp.getRequiredInterfaces().add(i1);
    });

    // ei1 and ei2 are on i1, but no functional exchange uses ei1 or ei2 => ko
    // test both sides, to verify variations between provided/required interfaces
    IStatus ls = unusedEIOnInterfaces(left.component, false);
    IStatus cs = unusedEIOnInterfaces(center.component, false);
    assertThat(ls, is(not(okStatus())));
    assertThat(cs, is(not(okStatus())));

    // the quickfix resolver should offer one solution for each ei: deallocate it from the interface
    IConstraintStatus ei1Status = ValidationHelpers.find(ei1, ls);
    assertThat(ei1Status, is(not(okStatus())));
    Collection<? extends Command> ei1ResolverCommands = resolver.generateResolutionCommands(ei1Status);
    assertThat(ei1ResolverCommands.size(), is(1));

    IConstraintStatus ei2Status = ValidationHelpers.find(ei2, ls);
    assertThat(ei2Status, is(not(okStatus())));
    Collection<? extends Command> ei2ResolverCommands = resolver.generateResolutionCommands(ei2Status);
    assertThat(ei2ResolverCommands.size(), is(1));

    // execute the resolver commands and revalidate
    executeCommand(() -> {
      ei1ResolverCommands.iterator().next().execute();
      ei2ResolverCommands.iterator().next().execute();
    });

    assertThat(unusedEIOnInterfaces(left.component, false), is(okStatus()));
    assertThat(unusedEIOnInterfaces(center.component, false), is(okStatus()));

    // undo the quickfix applications
    undo();

    // now create an fe that allocates ei1, ei2
    executeCommand(() -> {
      fe1_center_left = create(center.pf1.eContainer(), FE);
      allocate(ei1).on(fe1_center_left);
      allocate(ei2).on(fe1_center_left);
    });

    // ... and intentionally make the FE point in the 'wrong' direction.
    executeCommand(() -> {
      fe1_center_left.setSource(left.fop1_1);
      fe1_center_left.setTarget(center.fip1_1);
    });

    // since fe points in the opposite direction, it's not considered by the validation rule 
    assertThat(unusedEIOnInterfaces(left.component, false), is(not(okStatus())));
    assertThat(unusedEIOnInterfaces(center.component, false), is(not(okStatus())));
    
    // now make the FE point in the correct direction.
    executeCommand(() -> {
      fe1_center_left.setSource(center.fop1_1);
      fe1_center_left.setTarget(left.fip1_1);
    });

    // now both ei can be found on a suitable fe, so the rule remains quiet
    assertThat(unusedEIOnInterfaces(left.component, false), is(okStatus()));
    assertThat(unusedEIOnInterfaces(center.component, false), is(okStatus()));

    // allocate ei3 on the interface and the rule detects that the ei is missing on the exchanges
    executeCommand(() -> allocate(ei3).on(i1));

    ls = unusedEIOnInterfaces(left.component, false);
    assertThat(ls, is(not(okStatus())));
    
    cs = unusedEIOnInterfaces(center.component, false);
    assertThat(cs, is(not(okStatus())));

    IConstraintStatus ei3Status = ValidationHelpers.find(ei3, ls);
    assertThat(ei3Status, is(not(okStatus())));
    
    // Test the resolver:
    // - Two solutions must be offered
    //  * One deallocates ei3 from i
    //  * One allocates ei3 on fe
    Collection<? extends Command> resolutions = resolver.generateResolutionCommands(ei3Status);
    boolean eiDeallocatedFromInterface = false;
    boolean eiAllocatedOnExchange = false;
    assertThat(resolutions.size(), is(2));
    for (Iterator<? extends Command> it = resolutions.iterator(); it.hasNext();) {
      Command c = it.next();
      executeCommand(() -> c.execute());
      
      assertThat(unusedEIOnInterfaces(left.component, false), is(okStatus()));
      assertThat(unusedEIOnInterfaces(center.component, false), is(okStatus()));
      
      //test that ei3 on i <=> ei3 on fe
      if (i1.getExchangeItems().contains(ei3)) {
        assertThat(fe1_center_left.getExchangedItems(), hasItem(ei3));
        eiAllocatedOnExchange = true;
      } else {
        assertThat(fe1_center_left.getExchangedItems(), not(hasItem(ei3)));
        eiDeallocatedFromInterface = true;
      }
      undo();
    }
    assertTrue(eiDeallocatedFromInterface);
    assertTrue(eiAllocatedOnExchange);


    // now create a 2nd exchange that carries ei3, but make between the center and an unrelated right component.
    executeCommand(() -> { 
      fe2_center_left = create(center.pf1.eContainer(), FE);
      allocate(ei3).on(fe2_center_left);
      fe2_center_left.setSource(center.fop2_1);
      fe2_center_left.setTarget(right.fip1_1);
    });
    

    assertThat(unusedEIOnInterfaces(left.component, false), is(not(okStatus())));
    assertThat(unusedEIOnInterfaces(center.component, false), is(not(okStatus())));
    
    // now make the 2nd exchange connect the correct components and the rule should not fail no longer.
    executeCommand(() -> fe2_center_left.setTarget(left.fip2_1));

    assertThat(unusedEIOnInterfaces(left.component, false), is(okStatus()));
    assertThat(unusedEIOnInterfaces(center.component, false), is(okStatus()));
    
    

    /*
     * now we add some ei to the fe, and test the inverse: each ei on FE should be allocated to at least one of its related interfaces
     */
    
    // again test the inverse rule, with an additional ei allocation.
    // it is sufficient that one of the 2 interfaces allocates the ei:
    executeCommand(() -> {
      providerCp.getProvidedInterfaces().add(i2);
      requirerCp.getRequiredInterfaces().add(i2);
      allocate(ei4).on(fe2_center_left);
    });
    
    
    assertThat(unusedEIOnFunctionalExchanges(left.component, false), is(not(okStatus())));
    assertThat(unusedEIOnFunctionalExchanges(center.component, true), is(not(okStatus())));

    executeCommand(() -> {
      allocate(ei4).on(i2);
    });
    
    
    
    // allocate the ei on interface satisifies the allocation-agnostic rule, but not the one that uses port allocations
    assertThat(unusedEIOnFunctionalExchanges(left.component, false), is(okStatus()));
    assertThat(unusedEIOnFunctionalExchanges(center.component, false), is(okStatus()));

    
    assertThat(unusedEIOnFunctionalExchanges(left.component, true), is(not(okStatus())));
    assertThat(unusedEIOnFunctionalExchanges(center.component, true), is(not(okStatus())));

    /*
     * now test some variations when using the rule that takes fxp-cp allocations into account.
     */

    // i is between left.cp1 and center.cp1, but no fxp are allocated on these ports
    assertThat(unusedEIOnInterfaces(left.component, true), is(not(okStatus())));
    assertThat(unusedEIOnInterfaces(center.component, true), is(not(okStatus())));
    
    executeCommand(() -> {
      allocate(fe1_center_left.getSourceFunctionOutputPort()).on(requirerCp);
      allocate(fe1_center_left.getTargetFunctionInputPort()).on(providerCp);
    });
    
    // fe2 source/target are not allocated on cp yet, so it is not considered when searching for ei3
    assertThat(unusedEIOnInterfaces(left.component, true), is(not(okStatus())));
    assertThat(unusedEIOnInterfaces(center.component, true), is(not(okStatus())));

    executeCommand(()-> {
    allocate(fe2_center_left.getSourceFunctionOutputPort()).on(requirerCp);
    allocate(fe2_center_left.getTargetFunctionInputPort()).on(providerCp);
    });
    
    // fe1/2 source and target are now allocated on provider/requirer cp, all exchange items will be found on an fe
    assertThat(unusedEIOnInterfaces(left.component, true), is(okStatus()));
    assertThat(unusedEIOnInterfaces(center.component, true), is(okStatus()));
    
    // ... and the inverse rule that checks allocations will now also be satisfied
    assertThat(unusedEIOnFunctionalExchanges(left.component, true), is(okStatus()));
    assertThat(unusedEIOnFunctionalExchanges(center.component, true), is(okStatus()));

    // allocations do not impact dcon4x
    assertThat(unusedEIOnInterfaces(left.component, false), is(okStatus()));
    assertThat(unusedEIOnInterfaces(center.component, false), is(okStatus()));

    // and the inverse allocation based rule is also is also satisified now
    assertThat(unusedEIOnFunctionalExchanges(left.component, true), is(okStatus()));
    assertThat(unusedEIOnFunctionalExchanges(center.component, true), is(okStatus()));
    
    // now add an interface between center and right
    executeCommand(()->{
      allocate(ei5).on(i3);
      center.cp2.getRequiredInterfaces().add(i3);
      right.cp2.getProvidedInterfaces().add(i3);
    });
    
    
    assertThat(unusedEIOnInterfaces(center.component, true), is(not(okStatus())));
    assertThat(unusedEIOnInterfaces(center.component, false), is(not(okStatus())));
    assertThat(unusedEIOnInterfaces(right.component, true), is(not(okStatus())));
    assertThat(unusedEIOnInterfaces(right.component, false), is(not(okStatus())));

    // create exchange between center and right, allocating ei4..
    executeCommand(() -> {  
      fe3_center_right = create(center.pf1.eContainer(), FE);
      fe3_center_right.getExchangedItems().add(ei5);
      fe3_center_right.setSource(center.fop2_1);
      fe3_center_right.setTarget(right.fip2_1);
    });
  
    // satisfies the rule without allocations
    assertThat(unusedEIOnInterfaces(center.component, false), is(okStatus()));
    assertThat(unusedEIOnInterfaces(right.component, false), is(okStatus()));

    // but not the one that considers allocations  
    assertThat(unusedEIOnInterfaces(center.component, true), is(not(okStatus())));
    assertThat(unusedEIOnInterfaces(right.component, true), is(not(okStatus())));
    
    executeCommand(()->{
      allocate(fe3_center_right.getSourceFunctionOutputPort()).on(center.cp2);
      allocate(fe3_center_right.getTargetFunctionInputPort()).on(right.cp2);
    });
    
    // now allocations are established
    assertThat(unusedEIOnInterfaces(center.component, true), is(okStatus()));
    assertThat(unusedEIOnInterfaces(right.component, true), is(okStatus()));

  }

  
  protected IStatus unusedEIOnInterfaces(Component c, boolean considerAllocations){
    return ValidationHelpers.validate(c, considerAllocations ? NO_UNUSED_EI_ON_I_WITH_ALLOCATIONS : NO_UNUSED_EI_ON_I_ID);
  }

  protected IStatus unusedEIOnFunctionalExchanges(Component c, boolean considerAllocations){
    return ValidationHelpers.validate(c, considerAllocations ? NO_UNUSED_EI_ON_FE_WITH_ALLOCATIONS_ID : NO_UNUSED_EI_ON_FE_ID);
  }

}
