/*******************************************************************************
 * Copyright (c) 2017 THALES GLOBAL SERVICES.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *    Thales - initial API and implementation
 *******************************************************************************/
package org.polarsys.capella.test.libraries.ju.testcases.basic;

import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.polarsys.capella.core.libraries.model.CapellaModel;
import org.polarsys.capella.test.framework.api.BasicTestCase;
import org.polarsys.kitalpha.ad.metadata.metadata.Metadata;

/**
 * @author Thomas Guiu
 */
public class LibraryManager_removeReferenceToLibrary_metadata  extends BasicTestCase {

	  @SuppressWarnings("nls")
	  @Override
	  public List<String> getRequiredTestModels() {
	    return Arrays.asList("MyProject1", "MyLibrary1");
	  }

	  @SuppressWarnings("nls")
	  @Override
	  public void test() {
	    // -- SCENARIO -- //
	    CapellaModel monProjet1 = getTestModel("MyProject1");
	    CapellaModel maLibrairie1 = getTestModel("MyLibrary1");
	    monProjet1.addReference(maLibrairie1);
	    ResourceSet resourceSet = monProjet1.getEditingDomain().getResourceSet();
	    // -- ORACLE -- //
	    Metadata metadata = (Metadata)resourceSet.getEObject(URI.createURI("platform:/resource/MyProject1/MyProject1.afm#_UzLqMHnGEea0Df0HniJfuA"), true);
	    assertFalse(metadata.getAdditionalMetadata().isEmpty());
	    monProjet1.removeReference(maLibrairie1);
	    assertTrue(metadata.getAdditionalMetadata().isEmpty());
	  }

}
