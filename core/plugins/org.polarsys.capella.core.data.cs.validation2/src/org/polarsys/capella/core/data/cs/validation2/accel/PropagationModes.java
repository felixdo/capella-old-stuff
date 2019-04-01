package org.polarsys.capella.core.data.cs.validation2.accel;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.IParameterValues;

public class PropagationModes implements IParameterValues {

  @Override
  public Map<String,String> getParameterValues() {
    Map<String, String> result = new HashMap<>();
    result.put("union", "union");
    result.put("intersection", "intersection");
    result.put("selection", "selection");
    return result;
  }

}
