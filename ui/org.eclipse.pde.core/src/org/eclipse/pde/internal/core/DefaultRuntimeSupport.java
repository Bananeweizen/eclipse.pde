/*
 * Created on Sep 30, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.pde.internal.core;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.pde.core.*;
import org.eclipse.pde.core.plugin.IPluginModelBase;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DefaultRuntimeSupport implements IAlternativeRuntimeSupport {
	IWorkspaceModelManager workspaceModelManager;
	IExternalModelManager externalModelManager;

	public IWorkspaceModelManager getWorkspaceModelManager() {
		if (workspaceModelManager==null)
			workspaceModelManager = new WorkspaceModelManager();
		return workspaceModelManager;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.core.IAlternativeRuntimeSupport#getExternalModelManager()
	 */
	public IExternalModelManager getExternalModelManager() {
		if (externalModelManager==null)
			externalModelManager = new ExternalModelManager();
		return externalModelManager;
	}
	
	public IPath getPluginLocation(IPluginModelBase model) {
		String location = model.getInstallLocation();
		IResource resource = model.getUnderlyingResource();
		if (resource != null && resource.isLinked()) {
			// special case - linked resource
			location =
				resource
					.getLocation()
					.removeLastSegments(1)
					.addTrailingSeparator()
					.toString();
		}
		return new Path(location).addTrailingSeparator();
	}
	
	public void shutdown() {
		if (externalModelManager!=null) {
			externalModelManager.shutdown();
			externalModelManager=null;
		}
		if (workspaceModelManager!=null) {
			workspaceModelManager.shutdown();
			workspaceModelManager = null;
		}
	}
	

}
