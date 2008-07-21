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
package org.eclipse.pde.internal.ds.ui.editor.details;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.pde.internal.ds.core.IDSService;
import org.eclipse.pde.internal.ds.ui.Messages;
import org.eclipse.pde.internal.ds.ui.editor.DSInputContext;
import org.eclipse.pde.internal.ds.ui.editor.FormLayoutFactory;
import org.eclipse.pde.internal.ds.ui.editor.IDSMaster;
import org.eclipse.pde.internal.ds.ui.parts.ComboPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;

public class DSServiceDetails extends DSAbstractDetails {

	private IDSService fService;
	private Section fMainSection;
	private ComboPart fServiceFactory;
	private Label fLabelServiceFactory;

	public DSServiceDetails(IDSMaster masterSection) {
		super(masterSection, DSInputContext.CONTEXT_ID);
		fService = null;
		fMainSection = null;
	}

	public void createDetails(Composite parent) {

		Color foreground = getToolkit().getColors().getColor(IFormColors.TITLE);

		// Create main section
		fMainSection = getToolkit().createSection(parent,
				Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		fMainSection.clientVerticalSpacing = FormLayoutFactory.SECTION_HEADER_VERTICAL_SPACING;
		fMainSection.setText(Messages.DSServiceDetails_sectionTitle);
		fMainSection.setDescription(Messages.DSServiceDetails_sectionDescription);

		fMainSection.setLayout(FormLayoutFactory
				.createClearGridLayout(false, 1));

		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		fMainSection.setLayoutData(data);

		// Align the master and details section headers (misalignment caused
		// by section toolbar icons)
		alignSectionHeaders(fMainSection);

		// Create container for main section
		Composite mainSectionClient = getToolkit()
				.createComposite(fMainSection);
		mainSectionClient.setLayout(FormLayoutFactory
				.createSectionClientGridLayout(false, 2));

		// Attribute: LabelServiceFactory
		fLabelServiceFactory = getToolkit().createLabel(mainSectionClient,
				Messages.DSServiceDetails_serviceFactoryLabel, SWT.WRAP);
		fLabelServiceFactory.setForeground(foreground);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 20;
		gd.horizontalSpan = 1;
		gd.horizontalIndent = 3; // FormLayoutFactory.CONTROL_HORIZONTAL_INDENT
		
		// Attribute: ServiceFactory
		fServiceFactory = new ComboPart();
		fServiceFactory.createControl(mainSectionClient, getToolkit(),
				SWT.READ_ONLY);
		Control control = fServiceFactory.getControl();
		String[] items = new String[] { "true", "false" }; //$NON-NLS-1$ //$NON-NLS-2$
		fServiceFactory.setItems(items);
		fServiceFactory.getControl().setLayoutData(gd);

		// Bind widgets
		getToolkit().paintBordersFor(mainSectionClient);
		fMainSection.setClient(mainSectionClient);
		markDetailsPart(fMainSection);

	}

	public void hookListeners() {

		// Attribute: ServiceFactory
		fServiceFactory.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// Ensure data object is defined
				if (fService == null) {
					return;
				}

				fService
						.setServiceFactory(fServiceFactory.getSelectionIndex() == 0);
			}

		});
	}

	// }

	public void updateFields() {

		boolean editable = isEditableElement();
		// Ensure data object is defined
		if (fService == null) {
			return;
		}

		// Attribute: ServiceFactory
		fServiceFactory.select(fService.getServiceFactory() ? 0 : 1);

	}

	public void selectionChanged(IFormPart part, ISelection selection) {
		// Get the first selected object
		Object object = getFirstSelectedObject(selection);
		// Ensure we have the right type
		if ((object == null) || (object instanceof IDSService) == false) {
			return;
		}
		// Set data
		setData((IDSService) object);
		// Update the UI given the new data
		updateFields();
	}

	/**
	 * @param object
	 */
	public void setData(IDSService object) {
		// Set data
		fService = object;
	}
	


}
