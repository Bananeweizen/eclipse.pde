/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.internal;

import java.io.InputStream;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.api.tools.internal.provisional.IClassFile;

/**
 * A class file corresponding to a resource in the workspace.
 * 
 * @since 1.0
 */
public class ResourceClassFile extends AbstractClassFile {
	
	/**
	 * Corresponding file
	 */
	private IFile fFile;
	
	/**
	 * Fully qualified type name
	 */
	private String fTypeName;

	/**
	 * Constructs a class file on the underlying file.
	 * 
	 * @param file underlying resource
	 */
	public ResourceClassFile(IFile file, String typeName) {
		fFile = file;
		fTypeName = typeName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.internal.provisional.IClassFile#getInputStream()
	 */
	public InputStream getInputStream() throws CoreException {
		return fFile.getContents();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.internal.provisional.IClassFile#getTypeName()
	 */
	public String getTypeName() {
		return fTypeName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getTypeName();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return fTypeName.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if(obj instanceof IClassFile) {
			IClassFile file = (IClassFile) obj;
			return fTypeName.equals(file.getTypeName());
		}
		return super.equals(obj);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.internal.provisional.IClassFile#getURI()
	 */
	public URI getURI() {
		return fFile.getLocationURI();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.internal.provisional.IClassFile#getModificationStamp()
	 */
	public long getModificationStamp() {
		URI locationURI = fFile.getLocationURI();
		if (locationURI != null) {
			try {
				IFileStore store = EFS.getStore(locationURI);
				IFileInfo info = store.fetchInfo();
				return fFile.getModificationStamp() + info.getLength();
			} catch (CoreException e) {
			}
		}
		return fFile.getModificationStamp();
	}

}
