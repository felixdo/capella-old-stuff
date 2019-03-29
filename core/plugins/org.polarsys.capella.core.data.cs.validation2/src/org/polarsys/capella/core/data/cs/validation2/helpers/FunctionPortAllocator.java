package org.polarsys.capella.core.data.cs.validation2.helpers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.polarsys.capella.core.data.fa.ComponentPort;
import org.polarsys.capella.core.data.fa.FunctionPort;
import org.polarsys.capella.core.data.information.InformationFactory;
import org.polarsys.capella.core.data.information.PortAllocation;

public class FunctionPortAllocator {

  private final Collection<? extends FunctionPort> allocations;
  
  FunctionPortAllocator(Collection<? extends FunctionPort> eia){
    allocations = eia;
  }

  public FunctionPortAllocator on(ComponentPort cp) {
    for (FunctionPort fxp : allocations){
      if (!cp.getAllocatedFunctionPorts().contains(fxp)) { 
        PortAllocation pa = InformationFactory.eINSTANCE.createPortAllocation();
        cp.getOwnedPortAllocations().add(pa);
        pa.setSourceElement(cp);
        pa.setTargetElement(fxp);
      }
    }
    return this;
  }

  public static FunctionPortAllocator allocate(FunctionPort f){
    return allocate(Collections.singleton(f));
  }

  public static FunctionPortAllocator allocate(Collection<? extends FunctionPort> functions){
    return new FunctionPortAllocator(functions);
  }

  public static FunctionPortAllocator allocate(FunctionPort[] functionPorts) {
    return new FunctionPortAllocator(Arrays.asList(functionPorts));
  }

}
