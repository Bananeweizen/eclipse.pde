/*
 * Created on Mar 1, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.pde.internal.ui.neweditor.plugin;

import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.osgi.bundle.IBundleModel;
import org.eclipse.pde.core.plugin.IExtensionsModel;
import org.eclipse.pde.internal.core.osgi.bundle.*;
import org.eclipse.pde.internal.ui.neweditor.context.*;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PluginInputContextManager extends InputContextManager {
	private BundlePluginModelBase bmodel;
	/**
	 * 
	 */
	public PluginInputContextManager() {
	}

	public IModel getAggregateModel() {
		if (bmodel!=null)
			return bmodel;
		return findPluginModel();
	}
	
	protected void fireContextChange(InputContext context, boolean added) {
		super.fireContextChange(context, added);
		if (context.getId().equals(BundleInputContext.CONTEXT_ID)) {
			if (added)// bundle arriving
				bundleAdded(context);
			else
			// bundle going away
			bundleRemoved(context);
		}
		else if (context.getId().equals(BuildInputContext.CONTEXT_ID)) {
			if (added)
				buildAdded(context);
			else
				buildRemoved(context);
		}
		else if (context.getId().equals(PluginInputContext.CONTEXT_ID)) {
			if (added)
				pluginAdded(context);
			else
				pluginRemoved(context);
		}
	}
	private void bundleAdded(InputContext bundleContext) {
		IBundleModel model = (IBundleModel)bundleContext.getModel();
		if (model.isFragmentModel())
			bmodel = new BundleFragmentModel();
		else
			bmodel = new BundlePluginModel();
		bmodel.setBundleModel(model);
		syncExtensions();
	}
	
	private void syncExtensions() {
		IModel emodel = findPluginModel();
		if (emodel!=null && emodel instanceof IExtensionsModel)
			bmodel.setExtensionsModel((IExtensionsModel)emodel);
		else
			bmodel.setExtensionsModel(null);
	}
	
	private IModel findPluginModel() {
		InputContext pcontext = findContext(PluginInputContext.CONTEXT_ID);
		if (pcontext!=null)
			return pcontext.getModel();
		else
			return null;
	}

	private void bundleRemoved(InputContext bundleContext) {
		bmodel = null;
	}
	
	private void pluginAdded(InputContext pluginContext) {
		if (bmodel!=null)
			syncExtensions();
	}
	private void pluginRemoved(InputContext pluginContext) {
		if (bmodel!=null)
			syncExtensions();
	}
	private void buildAdded(InputContext buildContext) {
	}
	private void buildRemoved(InputContext buildContext) {
	}
}