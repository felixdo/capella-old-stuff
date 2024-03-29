/*******************************************************************************
 * Copyright (c) 2006, 2016 THALES GLOBAL SERVICES.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *    Thales - initial API and implementation
 *******************************************************************************/

package org.polarsys.capella.core.data.helpers.fa.delegates;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EStructuralFeature;

import org.polarsys.capella.core.data.fa.AbstractFunction;
import org.polarsys.capella.core.data.fa.FaPackage;
import org.polarsys.capella.core.data.fa.FunctionSpecification;
import org.polarsys.capella.core.data.helpers.capellacore.delegates.NamespaceHelper;
import org.polarsys.capella.common.data.activity.ActivityNode;
import org.polarsys.capella.common.data.helpers.activity.delegates.AbstractActivityHelper;

public class FunctionSpecificationHelper {
private static FunctionSpecificationHelper instance;
	
	private FunctionSpecificationHelper() {
    // do nothing
	}
	
	public static FunctionSpecificationHelper getInstance(){
		if(instance == null)
			instance = new FunctionSpecificationHelper();
		return instance;
	}

	public Object doSwitch(FunctionSpecification element, EStructuralFeature feature) {
		
		Object ret = null;
		
		if (feature.equals(FaPackage.Literals.FUNCTION_SPECIFICATION__SUB_FUNCTION_SPECIFICATIONS)) {
			ret = getSubFunctions(element);
		}

		// no helper found... searching in super classes...
    if(ret == null) {
      ret = NamespaceHelper.getInstance().doSwitch(element, feature);
    }
		if(ret == null) {
			ret = AbstractActivityHelper.getInstance().doSwitch(element, feature);
		}
		
		return ret;
	}

	protected List<FunctionSpecification> getSubFunctions(FunctionSpecification element) {
		List<ActivityNode> nodes = element.getOwnedNodes();
		List<FunctionSpecification> ret = new ArrayList<FunctionSpecification>();
		
		for (ActivityNode activityNode : nodes) {
			if(activityNode instanceof AbstractFunction){
				FunctionSpecification retFunc = ((AbstractFunction)activityNode).getLinkedFunctionSpecification();
				if(null != retFunc)
					ret.add(retFunc);
			}
		}
		
		return ret;
	}
}
