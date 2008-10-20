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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.pde.api.tools.internal.model.Reference;
import org.eclipse.pde.api.tools.internal.model.cache.TypeStructureCache;
import org.eclipse.pde.api.tools.internal.provisional.ApiPlugin;
import org.eclipse.pde.api.tools.internal.provisional.ClassFileContainerVisitor;
import org.eclipse.pde.api.tools.internal.provisional.IApiComponent;
import org.eclipse.pde.api.tools.internal.provisional.IClassFile;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiMember;
import org.eclipse.pde.api.tools.internal.provisional.model.IApiType;
import org.eclipse.pde.api.tools.internal.provisional.model.IReference;
import org.eclipse.pde.api.tools.internal.provisional.search.IApiSearchCriteria;
import org.eclipse.pde.api.tools.internal.provisional.search.IApiSearchEngine;
import org.eclipse.pde.api.tools.internal.provisional.search.IApiSearchResult;
import org.eclipse.pde.api.tools.internal.provisional.search.IApiSearchScope;
import org.eclipse.pde.api.tools.internal.provisional.search.ReferenceModifiers;
import org.eclipse.pde.api.tools.internal.util.Util;

import com.ibm.icu.text.MessageFormat;

/**
 * Extracts references from an API component.
 * 
 * @since 1.0.0
 */
public class SearchEngine implements IApiSearchEngine {
	
	/**
	 * Constant used for controlling tracing in the search engine
	 */
	private static boolean DEBUG = Util.DEBUG;
	
	/**
	 * Method used for initializing tracing in the search engine
	 */
	public static void setDebug(boolean debugValue) {
		DEBUG = debugValue || Util.DEBUG;
	}
	
	/**
	 * Empty result collection.
	 */
	private static final IApiSearchResult[] EMPTY_RESULT = new IApiSearchResult[0];
	
	/**
	 * Visits each class file, extracting references.
	 */
	class Visitor extends ClassFileContainerVisitor {
		
		private IApiComponent fCurrentComponent = null;
		private IProgressMonitor fMonitor = null;
		
		public Visitor(IProgressMonitor monitor) {
			fMonitor = monitor;
		}

		public void end(IApiComponent component) {
			fCurrentComponent = null;
		}

		public boolean visit(IApiComponent component) {
			fCurrentComponent = component;
			return true;
		}
		
		public boolean visitPackage(String packageName) {
			fMonitor.subTask(MessageFormat.format(SearchMessages.SearchEngine_0, new String[]{packageName}));
			return true;
		}

		public void endVisitPackage(String packageName) {
			fMonitor.worked(1);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.pde.api.tools.model.component.ClassFileContainerVisitor#visit(java.lang.String, org.eclipse.pde.api.tools.model.component.IClassFile)
		 */
		public void visit(String packageName, IClassFile classFile) {
			if (!fMonitor.isCanceled()) {
				try {
					IApiType type = TypeStructureCache.getTypeStructure(classFile, fCurrentComponent);
					List references = type.extractReferences(fAllReferenceKinds, null);
					// keep potential matches
					Iterator iterator = references.iterator();
					while (iterator.hasNext()) {
						IReference ref = (IReference) iterator.next();
						for (int i = 0; i < fConditions.length; i++) {
							if (fConditions[i].isPotentialMatch(ref)) {
								fPotentialMatches[i].add(ref);
							}
						}
					}
				} catch (CoreException e) {
					fStatus.add(e.getStatus());
				}
			}
		}
	}
		
	/**
	 * Scan status
	 */
	private MultiStatus fStatus;
		
	/**
	 * Potential matches for each search condition
	 */
	private List[] fPotentialMatches = null;
	
	/**
	 * Search criteria
	 */
	private IApiSearchCriteria[] fConditions = null;
	
	/**
	 * Mask of all reference kinds to consider based on all search conditions.
	 */
	private int fAllReferenceKinds = 0;
		
	/**
	 * Scans the given scope extracting all reference information.
	 * 
	 * @param scope scope to scan
	 * @param monitor progress monitor
	 * @exception CoreException if the scan fails
	 */
	private void extractReferences(IApiSearchScope scope, IProgressMonitor monitor) throws CoreException {
		fStatus = new MultiStatus(ApiPlugin.PLUGIN_ID, 0, SearchMessages.SearchEngine_1, null); 
		String[] packageNames = scope.getPackageNames();
		SubMonitor localMonitor = SubMonitor.convert(monitor, packageNames.length);
		ClassFileContainerVisitor visitor = new Visitor(localMonitor);
		long start = System.currentTimeMillis();
		try {
			scope.accept(visitor);
		} catch (CoreException e) {
			fStatus.add(e.getStatus());
		}
		long end = System.currentTimeMillis();
		if (!fStatus.isOK()) {
			throw new CoreException(fStatus);
		}
		localMonitor.done();
		if (DEBUG) {
			int size = 0;
			for (int i = 0; i < fPotentialMatches.length; i++) {
				size += fPotentialMatches[i].size();
			}
			System.out.println("Search: extracted " + size + " references in " + (end - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}
	
	/**
	 * Creates a unique string key for a given reference.
	 * The key is of the form "component X references type/member"
	 * <pre>
	 * [component_id]#[type_name](#[member_name]#[member_signature])
	 * </pre>
	 * @param reference reference
	 * @return a string key for the given reference.
	 */
	private String createSignatureKey(IReference reference) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(reference.getMember().getApiComponent().getId());
		buffer.append("#"); //$NON-NLS-1$
		buffer.append(reference.getReferencedTypeName());
		switch (reference.getReferenceType()) {
		case IReference.T_FIELD_REFERENCE:
			buffer.append("#"); //$NON-NLS-1$
			buffer.append(reference.getReferencedMemberName());
			break;
		case IReference.T_METHOD_REFERENCE:
			buffer.append("#"); //$NON-NLS-1$
			buffer.append(reference.getReferencedMemberName());
			buffer.append("#"); //$NON-NLS-1$
			buffer.append(reference.getReferencedSignature());
			break;
		}
		return buffer.toString();
	}
	
	/**
	 * Resolves all references.
	 * 
	 * @param referenceLists lists of {@link IReference} to resolve
	 * @param progress monitor
	 * @throws CoreException if something goes wrong
	 */
	private void resolveReferences(List[] referenceLists, IProgressMonitor monitor) throws CoreException {
		// sort references by target type for 'shared' resolution
		Map sigtoref = new HashMap(50);
		
		List refs = null;
		IReference ref = null;
		String key = null;
		List methodDecls = new ArrayList(1000);
		long start = System.currentTimeMillis();
		for (int i = 0; i < referenceLists.length; i++) {
			Iterator references = referenceLists[i].iterator();
			while (references.hasNext()) {
				ref = (IReference) references.next();
				if (ref.getReferenceKind() == ReferenceModifiers.REF_OVERRIDE) {
					methodDecls.add(ref);
				} else {
					key = createSignatureKey(ref);
					refs = (List) sigtoref.get(key);
					if(refs == null) {
						refs = new ArrayList(20);
						sigtoref.put(key, refs);
					}
					refs.add(ref);
				}
			}
		}
		if (monitor.isCanceled()) {
			return;
		}
		long end = System.currentTimeMillis();
		if (DEBUG) {
			System.out.println("Search: split into " + methodDecls.size() + " method overrides and " + sigtoref.size() + " unique references (" + (end - start) + "ms)");   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
		}
		// resolve references
		start = System.currentTimeMillis();
		resolveReferenceSets(sigtoref, monitor);
		end = System.currentTimeMillis();
		if (DEBUG) {
			System.out.println("Search: resolved unique references in " + (end - start) + "ms");  //$NON-NLS-1$//$NON-NLS-2$
		}
		// resolve method overrides
		start = System.currentTimeMillis();
		Iterator iterator = methodDecls.iterator();
		while (iterator.hasNext()) {
			Reference reference = (Reference) iterator.next();
			reference.resolve();
		}
		end = System.currentTimeMillis();
		if (DEBUG) {
			System.out.println("Search: resolved method overrides in " + (end - start) + "ms");  //$NON-NLS-1$//$NON-NLS-2$
		}	
	}
	
	/**
	 * Resolves the collect sets of references.
	 * @param map the mapping of keys to sets of {@link IReference}s
	 * @throws CoreException if something bad happens
	 */
	private void resolveReferenceSets(Map map, IProgressMonitor monitor) throws CoreException {
		Iterator types = map.keySet().iterator();
		String key = null;
		List refs = null;
		IReference ref= null;
		while (types.hasNext()) {
			if (monitor.isCanceled()) {
				return;
			}
			key = (String) types.next();
			refs = (List) map.get(key);
			ref = (IReference) refs.get(0);
			((org.eclipse.pde.api.tools.internal.model.Reference)ref).resolve();
			IApiMember resolved = ref.getResolvedReference();
			if (resolved != null) {
				Iterator iterator = refs.iterator();
				while (iterator.hasNext()) {
					org.eclipse.pde.api.tools.internal.model.Reference ref2 = (org.eclipse.pde.api.tools.internal.model.Reference) iterator.next();
					ref2.setResolution(resolved);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.search.IApiSearchEngine#search(org.eclipse.pde.api.tools.search.IApiSearchScope, int[], int[], int[], org.eclipse.pde.api.tools.search.IApiSearchScope, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IApiSearchResult[] search(IApiSearchScope sourceScope,
			IApiSearchCriteria[] conditions, IProgressMonitor monitor)
			throws CoreException {
		SubMonitor localMonitor = SubMonitor.convert(monitor,SearchMessages.SearchEngine_2, 3);
		fConditions = conditions;
		fPotentialMatches = new List[fConditions.length];
		for (int i = 0; i < conditions.length; i++) {
			IApiSearchCriteria condition = conditions[i];
			fAllReferenceKinds |= condition.getReferenceKinds();
			fPotentialMatches[i] = new LinkedList();
		}
		// 1. extract all references, filtering out kinds we don't care about
		localMonitor.subTask(SearchMessages.SearchEngine_3); 
		extractReferences(sourceScope, localMonitor);
		localMonitor.worked(1);
		if (localMonitor.isCanceled()) {
			return EMPTY_RESULT;
		}
		// 2. resolve the remaining references
		localMonitor.subTask(SearchMessages.SearchEngine_3);
		resolveReferences(fPotentialMatches, localMonitor);
		localMonitor.worked(1);
		if (localMonitor.isCanceled()) {
			return EMPTY_RESULT;
		}
		// 3. filter based on search conditions
		localMonitor.subTask(SearchMessages.SearchEngine_3);
		int emptyrefs = 0;
		for (int i = 0; i < fPotentialMatches.length; i++) {
			List references = fPotentialMatches[i];
			if (!references.isEmpty()) {
				IApiSearchCriteria condition = fConditions[i];
				applyConditions(references, condition);
				if(references.isEmpty()) {
					emptyrefs++;
				}
			}
			else {
				emptyrefs++;
			}
			if (localMonitor.isCanceled()) {
				return EMPTY_RESULT;
			}
		}
		int size = fPotentialMatches.length-emptyrefs;
		if(size <= 0) {
			return EMPTY_RESULT;
		}
		IApiSearchResult[] results = new IApiSearchResult[size];
		int index = 0;
		for (int i = 0; i < fPotentialMatches.length; i++) {
			List references = fPotentialMatches[i];
			if(references.isEmpty()) {
				continue;
			}
			results[index++] = new ApiSearchResult(fConditions[i], (IReference[]) references.toArray(new IReference[references.size()]));
			references.clear();
		}
		fPotentialMatches = null;
		localMonitor.worked(1);
		localMonitor.done();
		return results;
	}
	
	/**
	 * Iterates through the given references, removing those that do not match
	 * search conditions.
	 * 
	 * @param references
	 * @param condition condition to satisfy
	 */
	private void applyConditions(List references, IApiSearchCriteria condition) {
		Iterator iterator = references.iterator();
		while (iterator.hasNext()) {
			IReference ref = (IReference) iterator.next();
			if (!condition.isMatch(ref)) {
				iterator.remove();
			}
		}
	}
	
	public void resolveReferences(IReference[] references, IProgressMonitor monitor) throws CoreException {
		List list = new ArrayList(references.length);
		for (int i = 0; i < references.length; i++) {
			list.add(references[i]);
		}
		resolveReferences(new List[]{list}, monitor);
	}

}
