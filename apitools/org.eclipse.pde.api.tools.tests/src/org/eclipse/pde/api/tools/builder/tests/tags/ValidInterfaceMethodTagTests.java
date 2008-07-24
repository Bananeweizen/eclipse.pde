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
package org.eclipse.pde.api.tools.builder.tests.tags;

import junit.framework.Test;

import org.eclipse.core.runtime.IPath;

/**
 * Tests valid javadoc tags on interface methods
 * 
 * @since 3.5
 */
public class ValidInterfaceMethodTagTests extends ValidMethodTagTests {

	/**
	 * Constructor
	 * @param name
	 */
	public ValidInterfaceMethodTagTests(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.builder.tests.ApiBuilderTests#getTestSourcePath()
	 */
	protected IPath getTestSourcePath() {
		return super.getTestSourcePath().append("interface");
	}
	
	/**
	 * @return the test for this class
	 */
	public static Test suite() {
		return buildTestSuite(ValidInterfaceMethodTagTests.class);
	}
	
	/**
	 * Tests the supported @noreference tag on interface methods
	 * using an incremental build
	 */
	public void testValidInterfaceMethodTag1I() {
		deployIncrementalBuildTest(TESTING_PACKAGE, "test1", true);
	}
	
	/**
	 * Tests the supported @noreference tag on interface methods
	 * using a full build
	 */
	public void testValidInterfaceMethodTag1F() {
		deployFullBuildTest(TESTING_PACKAGE, "test1", true);
	}
	
	/**
	 * Tests the supported @noreference tag on outer interface methods
	 * using an incremental build
	 */
	public void testValidInterfaceMethodTag2I() {
		deployIncrementalBuildTest(TESTING_PACKAGE, "test2", true);
	}
	
	/**
	 * Tests the supported @noreference tag on outer interface methods
	 * using a full build
	 */
	public void testValidInterfaceMethodTag2F() {
		deployFullBuildTest(TESTING_PACKAGE, "test2", true);
	}
	
	/**
	 * Tests the supported @noreference tag on inner interface methods
	 * using an incremental build
	 */
	public void testValidInterfaceMethodTag3I() {
		deployIncrementalBuildTest(TESTING_PACKAGE, "test3", true);
	}
	
	/**
	 * Tests the supported @noreference tag on inner interface methods
	 * using a full build
	 */
	public void testValidInterfaceMethodTag3F() {
		deployFullBuildTest(TESTING_PACKAGE, "test3", true);
	}
	
	/**
	 * Tests the supported @noreference tag on a variety of inner / outer interface methods
	 * using an incremental build
	 */
	public void testValidInterfaceMethodTag4I() {
		deployIncrementalBuildTest(TESTING_PACKAGE, "test4", true);
	}
	
	/**
	 * Tests the supported @noreference tag on a variety of inner / outer interface methods
	 * using a full build
	 */
	public void testValidInterfaceMethodTag4F() {
		deployFullBuildTest(TESTING_PACKAGE, "test4", true);
	}
	
	/**
	 * Tests the supported @noreference tag on interface methods in the default package
	 * using an incremental build
	 */
	public void testValidInterfaceMethodTag5I() {
		deployIncrementalBuildTest("", "test5", true);
	}
	
	/**
	 * Tests the supported @noreference tag on interface methods in the default package
	 * using a full build
	 */
	public void testValidInterfaceMethodTag5F() {
		deployFullBuildTest("", "test5", true);
	}
}
