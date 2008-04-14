/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Chris Aniszczyk <caniszczyk@gmail.com>
 *     Rafael Oliveira N�brega <rafael.oliveira@gmail.com> - bug 223739
 *******************************************************************************/
package org.eclipse.pde.internal.ds.ui.editor;


import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.pde.core.IBaseModel;
import org.eclipse.pde.internal.ds.core.text.DSModel;
import org.eclipse.pde.internal.ui.editor.JarEntryEditorInput;
import org.eclipse.pde.internal.ui.editor.PDEFormEditor;
import org.eclipse.pde.internal.ui.editor.SystemFileEditorInput;
import org.eclipse.pde.internal.ui.editor.context.XMLInputContext;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;

/**
 * DSInputContext
 *
 */
public class DSInputContext extends XMLInputContext {

	public static final String CONTEXT_ID = "ds-context"; //$NON-NLS-1$	

	/**
	 * @param editor
	 * @param input
	 * @param primary
	 */
	public DSInputContext(PDEFormEditor editor, IEditorInput input, boolean primary) {
		super(editor, input, primary);
		create();
	}

	protected void reorderInsertEdits(ArrayList ops) {
		// no op
		
	}

	protected IBaseModel createModel(IEditorInput input) throws CoreException {
		if ((input instanceof IStorageEditorInput) == false) {
			return null;
		}

		boolean isReconciling = false;
		if (input instanceof IFileEditorInput) {
			isReconciling = true;
		}
		
		IDocument document = getDocumentProvider().getDocument(this);
		DSModel model = new DSModel(document, isReconciling);

		if (input instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput) input).getFile();
			model.setUnderlyingResource(file);
			model.setCharset(file.getCharset());
		} else if (input instanceof SystemFileEditorInput) {
			File file = (File) ((SystemFileEditorInput) input).getAdapter(File.class);
			model.setInstallLocation(file.getParent());
			model.setCharset(getDefaultCharset());
		} else if (input instanceof JarEntryEditorInput) {
			File file = (File) ((JarEntryEditorInput) input).getAdapter(File.class);
			model.setInstallLocation(file.toString());
			model.setCharset(getDefaultCharset());
		} else {
			model.setCharset(getDefaultCharset());
		}

		
		model.load();
		
		return model;
	}

	public String getId() {
		return CONTEXT_ID;
	}

	protected String getPartitionName() {
		return "___ds_partition"; 
	}

}
