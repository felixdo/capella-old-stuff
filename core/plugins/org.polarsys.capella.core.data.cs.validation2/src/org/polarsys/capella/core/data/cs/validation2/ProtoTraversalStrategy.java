package org.polarsys.capella.core.data.cs.validation2;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.validation.service.AbstractTraversalStrategy;

final class ProtoTraversalStrategy extends AbstractTraversalStrategy {

 private Collection<EObject> roots;
 private boolean contextChanged = true;

 
 @Override
     public void startTraversal(
     Collection<? extends EObject> traversalRoots,
     IProgressMonitor progressMonitor) {
   
   
//   EObject context = traversalRoots.iterator().next();
//   ()
//   
//   Component[] comps = new Component[2];
//   Iterator<Component> it = (Iterator<Component>) traversalRoots.iterator();
//   for (int i = 0; i < comps.length; i++) {
//     if (it.hasNext()) {
//       comps[i] = it.next();
//     }
//   }
//   if (comps[1] != null) {
//     
//   }
//   
   roots = makeTargetsDisjoint(traversalRoots);
   
   
   super.startTraversal(traversalRoots, progressMonitor);
 }
 
 private Collection<EObject> getRoots() {
   return roots;
 }
 
 /* (non-Javadoc)
  * Implements the inherited method.
  */
 @Override
     protected int countElements(Collection<? extends EObject> ignored) {
   return countRecursive(getRoots());
 }
 
 private int countRecursive(Collection<? extends EObject> elements) {
   int result = 0;
   
   result = elements.size();
   
   for (EObject next : elements) {
     result = result + countRecursive(next.eContents());
   }
   
   return result;
 }
 
 /* (non-Javadoc)
  * Implements the inherited method.
  */
 @Override
     protected Iterator<? extends EObject> createIterator(
             Collection<? extends EObject> ignored) {
     
   return new EcoreUtil.ContentTreeIterator<EObject>(getRoots()) {
     private static final long serialVersionUID = -5653134989235663973L;

     @Override
             public Iterator<EObject> getChildren(Object obj) {
       if (obj == getRoots()) {
         return new Iterator<EObject>() {
           private final Iterator<EObject> delegate =
             getRoots().iterator();
           
           public boolean hasNext() {
             return delegate.hasNext();
           }

           public EObject next() {
             // if I'm being asked for my next element, then
             //    we are stepping to another traversal root
             contextChanged = true;
             
             return delegate.next();
           }

           public void remove() {
             delegate.remove();
           }};
       } else {
         return super.getChildren(obj);
       }
     }
     
     @Override
             public EObject next() {
       // this will be set to true again the next time we test hasNext() at
       //    the traversal root level
       contextChanged = false;
       
       return super.next();
     }};
 }
 
 @Override
     public boolean isClientContextChanged() {
   return contextChanged;
 }
 
 private Set<EObject> makeTargetsDisjoint(Collection<? extends EObject> objects) {
   Set<EObject> result = new java.util.HashSet<EObject>();
   
   // ensure that any contained (descendent) elements of other elements
   //    that we include are not included, because they will be
   //    traversed by recursion, anyway
   
       for (EObject target : objects) {
           // EcoreUtil uses the InternalEObject interface to check
           // containment, so we do the same.  Also, we kip up a level to
           // the immediate container for efficiency:  an object is its
           // own ancestor, so we can "pre-step" up a level to avoid the
           // cost of doing it individually for every miss in the collection
           if (!EcoreUtil.isAncestor(objects,
                   ((InternalEObject) target).eInternalContainer())) {
               result.add(target);
           }
       }
   
   return result;
 }

 
}
