package org.polarsys.capella.core.data.cs.validation2;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.validation.model.EvaluationMode;
import org.eclipse.emf.validation.model.IConstraintStatus;
import org.eclipse.emf.validation.service.IBatchValidator;
import org.eclipse.emf.validation.service.ModelValidationService;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManager;
import org.eclipse.ui.handlers.HandlerUtil;
import org.polarsys.capella.common.helpers.validation.ConstraintStatusDiagnostic;
import org.polarsys.capella.common.tools.report.appenders.reportlogview.LightMarkerRegistry;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.model.handler.markers.ICapellaValidationConstants;

public class ValidateInterfacesHandler extends AbstractHandler {

  @SuppressWarnings("unchecked")
  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    IBatchValidator validator = ModelValidationService.getInstance().newValidator(EvaluationMode.BATCH);
    validator.setTraversalStrategy(new ProtoTraversalStrategy());
    
    List<? extends Component> selection = HandlerUtil.getCurrentStructuredSelection(event).toList();
    Component left = selection.get(0);
    Component right = selection.size() == 2 ? selection.get(1) : null;

    EObject context = InterfaceValidationPackage.createInterfaceValidationContext(left, right);    
    IStatus result = validator.validate(context);
 
    BasicDiagnostic chain = new BasicDiagnostic();
    appendDiagnostics(result, chain);
    for (Diagnostic diagnostic : chain.getChildren()) {
      IResource resource = null;
      for (Object o : diagnostic.getData()) {
        if (o instanceof EObject) {
          if ((resource = getFile((EObject) o)) != null) {
            break;
          }
        }
      }
      if (resource != null) {
        LightMarkerRegistry.getInstance().createMarker(resource, diagnostic, ICapellaValidationConstants.CAPELLA_MARKER_ID);
      }
    }

    return null;
  }

  protected void appendDiagnostics(IStatus status, DiagnosticChain diagnostics_p) {
    if (status.isMultiStatus()) {
      IStatus[] children = status.getChildren();
      for (IStatus element : children) {
        appendDiagnostics(element, diagnostics_p);
      }
    } else if (status instanceof IConstraintStatus) {
      diagnostics_p.add(new ConstraintStatusDiagnostic((IConstraintStatus) status));
    }
  }

  protected IResource getFile(EObject datum) {
    Session session = SessionManager.INSTANCE.getSession((EObject) datum);
    if (session != null) {
      Resource resource = session.getSessionResource();
      URI uri = resource.getResourceSet().getURIConverter().normalize(resource.getURI());
      if (uri.isPlatformResource() && uri.segmentCount() > 2) {
        return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(uri.toPlatformString(true)));
      }
    }
    return null;    

  }

  
}
