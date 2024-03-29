/*******************************************************************************
 * Copyright (c) 2006, 2015 THALES GLOBAL SERVICES.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *    Thales - initial API and implementation
 *******************************************************************************/
package org.polarsys.capella.common.data.helpers.activity.delegates;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.polarsys.capella.common.data.activity.ActivityEdge;
import org.polarsys.capella.common.data.activity.ActivityNode;
import org.polarsys.capella.common.data.activity.ActivityPackage;
import org.polarsys.capella.common.data.activity.ActivityPartition;
import org.polarsys.capella.common.data.activity.InterruptibleActivityRegion;
import org.polarsys.capella.common.data.activity.StructuredActivityNode;
import org.polarsys.capella.common.helpers.EObjectExt;

public class ActivityNodeHelper {
  private static ActivityNodeHelper instance;

  private ActivityNodeHelper() {
  }

  public static ActivityNodeHelper getInstance() {
    if (instance == null) {
      instance = new ActivityNodeHelper();
    }
    return instance;
  }

  public Object doSwitch(ActivityNode element, EStructuralFeature feature) {
    Object ret = null;

    if (feature.equals(ActivityPackage.Literals.ACTIVITY_NODE__IN_ACTIVITY_PARTITION)) {
      ret = getInActivityPartition(element);
    } else if (feature.equals(ActivityPackage.Literals.ACTIVITY_NODE__IN_INTERRUPTIBLE_REGION)) {
      ret = getInInterruptibleRegion(element);
    } else if (feature.equals(ActivityPackage.Literals.ACTIVITY_NODE__IN_STRUCTURED_NODE)) {
      ret = getInStructuredNode(element);
    } else if (feature.equals(ActivityPackage.Literals.ACTIVITY_NODE__INCOMING)) {
      ret = getIncomingEdges(element);
    } else if (feature.equals(ActivityPackage.Literals.ACTIVITY_NODE__OUTGOING)) {
      ret = getOutgoingEdges(element);
    }

    return ret;
  }

  protected ActivityPartition getInActivityPartition(ActivityNode element) {
    EObject group = element.eContainer();
    if ((null != group) && (group instanceof ActivityPartition)) {
      return (ActivityPartition) group;
    }
    return null;
  }

  protected InterruptibleActivityRegion getInInterruptibleRegion(ActivityNode element) {
    EObject group = element.eContainer();
    if ((null != group) && (group instanceof InterruptibleActivityRegion)) {
      return (InterruptibleActivityRegion) group;
    }
    return null;
  }

  protected StructuredActivityNode getInStructuredNode(ActivityNode element) {
    EObject group = element.eContainer();
    if ((null != group) && (group instanceof StructuredActivityNode)) {
      return (StructuredActivityNode) group;
    }
    return null;
  }

  protected List<ActivityEdge> getIncomingEdges(ActivityNode element) {
    return EObjectExt.getReferencers(element, ActivityPackage.Literals.ACTIVITY_EDGE__TARGET);
  }

  protected List<ActivityEdge> getOutgoingEdges(ActivityNode element) {
    return EObjectExt.getReferencers(element, ActivityPackage.Literals.ACTIVITY_EDGE__SOURCE);
  }
}
