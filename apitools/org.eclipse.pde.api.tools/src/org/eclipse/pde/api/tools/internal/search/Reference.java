/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.internal.search;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.api.tools.internal.model.TypeStructureCache;
import org.eclipse.pde.api.tools.internal.provisional.IApiAnnotations;
import org.eclipse.pde.api.tools.internal.provisional.IApiComponent;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IMethodDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.descriptors.IReferenceTypeDescriptor;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiMethod;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiType;
import org.eclipse.pde.api.tools.internal.provisional.search.ILocation;
import org.eclipse.pde.api.tools.internal.provisional.search.IReference;
import org.eclipse.pde.api.tools.internal.provisional.search.ReferenceModifiers;
import org.eclipse.pde.api.tools.internal.util.ClassFileResult;
import org.eclipse.pde.api.tools.internal.util.Util;

/**
 * Base Implementation of {@link IReference}
 * 
 * @since 1.0.0
 */
public class Reference implements IReference {

	private ILocation target = null;
	private ILocation source = null;
	private ILocation resolved = null; 
	private int kind;
	private IApiAnnotations description = null;
	
	/**
	 * Bit mask of method reference kinds that need resolution 
	 */
	private final static int METHODS_TO_RESOLVE =
			ReferenceModifiers.REF_VIRTUALMETHOD | 	// these have to be resolved to see where they are implemented
			ReferenceModifiers.REF_OVERRIDE |		// resolve inherited methods to see if @noextend
			ReferenceModifiers.REF_STATICMETHOD |	// resolve static, as could be synthetic
			ReferenceModifiers.REF_SPECIALMETHOD | 	// resolve specials (super)
			ReferenceModifiers.REF_INTERFACEMETHOD; // resolves interface methods
	
	/**
	 * Constructor
	 * @param source the source of the reference
	 * @param target the target of the reference
	 * @param kind the kind of the reference. See {@link IReference} for a listing of kinds
	 */
	public Reference(ILocation source, ILocation target, int kind) {
		this.source = source;
		this.target = target;
		this.kind = kind;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.search.IReference#getReferenceKind()
	 */
	public int getReferenceKind() {
		return kind;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.search.IReference#getSourceLocation()
	 */
	public ILocation getSourceLocation() {
		return source;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.search.IReference#getTargetApiDescription()
	 */
	public IApiAnnotations getResolvedAnnotations() {
		return description;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.search.IReference#getTargetLocation()
	 */
	public ILocation getReferencedLocation() {
		return target;
	}
	
	/**
	 * Resolves this reference in the given profile.
	 * 
	 * @param engine search engine resolving the reference
	 * @throws CoreException
	 */
	void resolve(SearchEngine engine) throws CoreException {
		if(resolved == null) {
			IApiComponent sourceComponent = source.getApiComponent();
			if(sourceComponent != null) {
				if ((getReferenceKind() & METHODS_TO_RESOLVE) > 0) {
					IMethodDescriptor method = (IMethodDescriptor) target.getMember();
					resolveVirtualMethod(sourceComponent, method);
				} else {
					ClassFileResult result = Util.getComponent(
							sourceComponent.getProfile().resolvePackage(sourceComponent, target.getType().getPackage().getName()),
							target.getType().getQualifiedName());
					if(result != null) {
						ILocation res = new Location(result.getComponent(), target.getMember());
						res.setLineNumber(target.getLineNumber());
						IApiAnnotations ann = result.getComponent().getApiDescription().resolveAnnotations(res.getMember());
						setResolution(ann, res);
					}
				}
			}
		}
		// TODO: throw exception on failure
	}	
	
	/**
	 * Resolves a virtual method and returns whether the method lookup was successful.
	 * We need to resolve the actual type that implements the method - i.e. do the virtual
	 * method lookup.
	 * 
	 * @param profile profile in which method lookup is to be resolved
	 * @param callSiteComponent the component where the method call site was located
	 * @param method the method that has been called
	 * @returns whether the lookup succeeded
	 * @throws CoreException if something goes terribly wrong
	 */
	private boolean resolveVirtualMethod(IApiComponent callSiteComponent, IMethodDescriptor method) throws CoreException {
		// resolve the package in which to start the lookup
		IApiComponent[] implComponents = callSiteComponent.getProfile().resolvePackage(callSiteComponent, method.getPackage().getName());
		String receivingTypeName = method.getEnclosingType().getQualifiedName();
		ClassFileResult result = Util.getComponent(implComponents, receivingTypeName);
		if (result != null) {
			IApiType structure = TypeStructureCache.getTypeStructure(result.getClassFile(), result.getComponent());
			if (structure != null) {
				IApiMethod target = structure.getMethod(method.getName(), method.getSignature());
				if (target != null) {
					if (target.isSynthetic()) {
						// don't resolve references to synthetic methods
						return false;
					} else {
						ILocation res = new Location(result.getComponent(), method);
						IApiAnnotations ann = result.getComponent().getApiDescription().resolveAnnotations(
								res.getMember());
						setResolution(ann, res);
						return true;
					}
				}
				if (kind == ReferenceModifiers.REF_INTERFACEMETHOD) {
					// resolve method in super interfaces rather than class
					String[] interaces = structure.getSuperInterfaceNames();
					if (interaces != null) {
						for (int i = 0; i < interaces.length; i++) {
							IReferenceTypeDescriptor supertype = Util.getType(interaces[i]);
							if (resolveVirtualMethod(result.getComponent(), supertype.getMethod(method.getName(), method.getSignature()))) {
								return true;
							}
						}
					}
				} else {
					String superName = structure.getSuperclassName();
					if (superName != null) {
						IReferenceTypeDescriptor supertype = Util.getType(superName);
						return resolveVirtualMethod(result.getComponent(), supertype.getMethod(method.getName(), method.getSignature()));
					}
				}
			}
		}
		return false;
	}	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String kindstr = Util.getReferenceKind(this.kind);
		if(kindstr == null) {
			kindstr = "UNKNOWN_KIND"; //$NON-NLS-1$
		}
		StringBuffer ms = new StringBuffer();
		ms.append(source.getMember().toString());
		ms.append(" references "); //$NON-NLS-1$
		ms.append(target.getMember().toString());
		ms.append(" via "); //$NON-NLS-1$
		ms.append(kindstr);
		int lineNumber = getSourceLocation().getLineNumber();
		if (lineNumber != -1) {
			ms.append(" [line: "); //$NON-NLS-1$
			ms.append(lineNumber);
			ms.append("]"); //$NON-NLS-1$
		}
		return ms.toString();
	}
	
	/**
	 * Sets the resolution of this reference.
	 * 
	 * @param resolution API description
	 * @param targetLocation resolved target location
	 */
	void setResolution(IApiAnnotations resolution, ILocation targetLocation) {
		description = resolution;
		resolved = targetLocation;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Reference) { 
			Reference ref = (Reference) obj;
			return getReferenceKind() == ref.getReferenceKind() &&
				equalOrNull(getResolvedLocation(), ref.getResolvedLocation()) &&
				getSourceLocation().equals(ref.getSourceLocation()) &&
				getReferencedLocation().equals(ref.getReferencedLocation()) &&
				Util.equalsOrNull(getResolvedAnnotations(), ref.getResolvedAnnotations());
		}
		return false;
	}
	
	private boolean equalOrNull(ILocation l1, ILocation l2) {
		if (l1 == null) {
			return l2 == null;
		}
		return l1.equals(l2);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return kind + source.hashCode() + target.hashCode();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.internal.provisional.search.IReference#getResolvedLocation()
	 */
	public ILocation getResolvedLocation() {
		return resolved;
	}

	
}