package org.polarsys.capella.core.data.cs.validation2;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.sirius.diagram.ui.edit.api.part.IDiagramElementEditPart;
import org.eclipse.sirius.viewpoint.DSemanticDecorator;
import org.polarsys.capella.common.data.modellingcore.ModelElement;

public class IDiagramElementEditPartAdapterFactory implements IAdapterFactory {

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
    if (adapterType == ModelElement.class) {
      Object model = ((IDiagramElementEditPart) adaptableObject).getModel();
      if (model instanceof View) {
        Object element = ((View)model).getElement();
        if (element instanceof DSemanticDecorator && adapterType.isInstance(((DSemanticDecorator)element).getTarget())) {
          return (T) ((DSemanticDecorator)element).getTarget();
        }
      }
    }
    return null;
  }

  @Override
  public Class<?>[] getAdapterList() {
    return new Class[] {
        ModelElement.class
    };
  }

}
