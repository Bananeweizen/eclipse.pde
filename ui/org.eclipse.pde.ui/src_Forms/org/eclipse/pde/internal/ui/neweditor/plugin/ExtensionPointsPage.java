/*
 * Created on Jan 27, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.pde.internal.ui.neweditor.plugin;

import org.eclipse.pde.internal.ui.neweditor.PDEFormPage;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.*;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionPointsPage extends PDEFormPage {
	/**
	 * @param editor
	 * @param id
	 * @param title
	 */
	public ExtensionPointsPage(FormEditor editor) {
		super(editor, "ex-points", "Extension Points");
	}
	
	protected void createFormContent(ManagedForm managedForm) {
		super.createFormContent(managedForm);
		Form form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
		form.setText("Extension Points");
	}
}