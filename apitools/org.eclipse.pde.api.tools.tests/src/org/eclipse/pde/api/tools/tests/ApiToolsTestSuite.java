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
package org.eclipse.pde.api.tools.tests;


import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.pde.api.tools.builder.tests.OSGiLessAnalysisTests;
import org.eclipse.pde.api.tools.comparator.tests.AllDeltaTests;
import org.eclipse.pde.api.tools.model.tests.ApiDescriptionTests;
import org.eclipse.pde.api.tools.model.tests.ApiBaselineTests;
import org.eclipse.pde.api.tools.model.tests.ClassFileContainerTests;
import org.eclipse.pde.api.tools.model.tests.ClassFileScannerTests;
import org.eclipse.pde.api.tools.model.tests.ComponentManifestTests;
import org.eclipse.pde.api.tools.model.tests.ElementDescriptorTests;
import org.eclipse.pde.api.tools.model.tests.TagScannerTests;
import org.eclipse.pde.api.tools.problems.tests.ApiProblemFactoryTests;
import org.eclipse.pde.api.tools.problems.tests.ApiProblemTests;
import org.eclipse.pde.api.tools.reference.tests.SearchEngineTests;
import org.eclipse.pde.api.tools.reference.tests.SearchScopeTests;
import org.eclipse.pde.api.tools.util.tests.HeadlessApiProfileManagerTests;
import org.eclipse.pde.api.tools.util.tests.UtilTests;

/**
 * Test suite for all of the API tools test 
 * 
 * @since 1.0.0
 */
public class ApiToolsTestSuite extends TestSuite {

	/**
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 * @return the test
	 */
	public static Test suite() {
		return new ApiToolsTestSuite();
	}
	
	/**
	 * Constructor
	 */
	public ApiToolsTestSuite() {
		addTest(new TestSuite(ApiDescriptionTests.class));
		addTest(new TestSuite(HeadlessApiProfileManagerTests.class));
		addTest(new TestSuite(TagScannerTests.class));
		addTest(new TestSuite(ComponentManifestTests.class));
		addTest(new TestSuite(UtilTests.class));
		addTest(new TestSuite(ApiBaselineTests.class));
		addTest(new TestSuite(ClassFileContainerTests.class));
		addTest(new TestSuite(ClassFileScannerTests.class));
		addTest(new TestSuite(ComponentManifestTests.class));
		addTest(new AllDeltaTests());
		addTest(new TestSuite(ElementDescriptorTests.class));
		addTest(new TestSuite(SearchScopeTests.class));
		addTest(new TestSuite(SearchEngineTests.class));
		addTest(new TestSuite(ApiProblemTests.class));
		addTest(new TestSuite(ApiProblemFactoryTests.class));
		addTest(new TestSuite(OSGiLessAnalysisTests.class));
		//addTest(new TestSuite(ExportComponentTests.class));
		/*addTest(new TestSuite(DiscouragedAccessTests.class));*/
	}	
}
