/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.ui.internal.actions;

import org.eclipse.osgi.util.NLS;

/**
 * 
 */
public class ActionMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.pde.api.tools.ui.internal.actions.actionmessages"; //$NON-NLS-1$
	public static String ApiToolingSetupObjectContribution_0;
	public static String SelectABaseline;
	public static String SetAsDefault;
	public static String EnterFileName;
	public static String SelectFileName;
	public static String Browse;
	public static String ConvertToHtml;
	public static String CompareDialogTitle;
	public static String CompareDialogCollectingElementTaskName;
	public static String CompareDialogComputeDeltasTaskName;
	public static String CompareWithAction_comparing_apis;
	public static String CompareTaskNoChanges;
	public static String RemoveActiveSessionAction_label;
	public static String RemoveActiveSessionAction_tooltip;
	public static String RemoveAllSessionsAction_label;
	public static String RemoveAllSessionsAction_tooltip;
	public static String SelectSessionAction_label;
	public static String SelectSessionAction_tooltip;
	public static String SelectSessionActionEntry_label;
	public static String DeltaDetailsDialogTitle;
	public static String ExportSessionAction_label;
	public static String ExportSessionAction_tooltip;
	public static String EnterFileNameForExport;
	public static String ExportActionTitle;
	public static String ExportDialogDescription;
	public static String PropertyPackageVisibility;

	public static String PropertyMessageKey;
	public static String PropertyComponentKey;
	public static String PropertyElementTypeKey;
	public static String PropertyFlagsKey;
	public static String PropertyKeyKey;
	public static String PropertyKindKey;
	public static String PropertyNewModifiersKey;
	public static String PropertyOldModifiersKey;
	public static String PropertyRestrictionsKey;
	public static String PropertyTypeNameKey;

	public static String MessageCategory;
	public static String InfoCategory;
	public static String ExportDialogErrorMessage;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, ActionMessages.class);
	}

	private ActionMessages() {
	}
}
