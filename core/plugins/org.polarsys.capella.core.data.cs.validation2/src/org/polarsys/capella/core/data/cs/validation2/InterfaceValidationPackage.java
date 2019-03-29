package org.polarsys.capella.core.data.cs.validation2;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.cs.CsPackage;

/**
 * A dynamic helper model to allow contextual interface validation of components
 */
@SuppressWarnings("nls")
class InterfaceValidationPackage {
  
  private static final EClass INTERFACE_VALIDATION_CONTEXT;
  private static final EReference LEFT;
  private static final EReference RIGHT;
  
  static {
    EPackage pack = EcoreFactory.eINSTANCE.createEPackage();
    pack.setNsURI("http://www.polarsys.org/capella/interfaceValidator");

    INTERFACE_VALIDATION_CONTEXT = EcoreFactory.eINSTANCE.createEClass();
    INTERFACE_VALIDATION_CONTEXT.setName("ValidationContext");

    LEFT = EcoreFactory.eINSTANCE.createEReference();
    LEFT.setName("leftComponent");
    LEFT.setEType(CsPackage.Literals.COMPONENT);

    RIGHT = EcoreFactory.eINSTANCE.createEReference();
    RIGHT.setName("rightComponent");
    RIGHT.setEType(CsPackage.Literals.COMPONENT);

    INTERFACE_VALIDATION_CONTEXT.getEReferences().add(LEFT);
    INTERFACE_VALIDATION_CONTEXT.getEReferences().add(RIGHT);
  }

  public static EObject createInterfaceValidationContext(Component left, Component right) {
    EObject result = EcoreUtil.create(INTERFACE_VALIDATION_CONTEXT);
    result.eSet(LEFT, left);
    result.eSet(RIGHT, right);
    return result;
  }
  
  public static Component getRight(EObject interfaceValidationContext) {
    return (Component) interfaceValidationContext.eGet(RIGHT);
  }
  
  public static Component getLeft(EObject interfaceValidationContext) {
    return (Component) interfaceValidationContext.eGet(LEFT);
  }


}
