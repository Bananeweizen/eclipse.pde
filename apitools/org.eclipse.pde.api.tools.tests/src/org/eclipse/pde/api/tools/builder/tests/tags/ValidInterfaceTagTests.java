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

import org.eclipse.core.runtime.IPath;

import junit.framework.Test;

/**
 * Tests the tags that are valid on an interface
 * 
 * @since 3.4
 */
public class ValidInterfaceTagTests extends InvalidInterfaceTagTests {

	/**
	 * Constructor
	 * @param name
	 */
	public ValidInterfaceTagTests(String name) {
		super(name);
	}

	/**
	 * @return the tests for this class
	 */
	public static Test suite() {
		return buildTestSuite(ValidInterfaceTagTests.class);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.builder.tests.InvalidJavadocTagClassTests#getTestSourcePath()
	 */
	protected IPath getTestSourcePath() {
		return super.getTestSourcePath().append("valid");
	}
	
	/**
	 * Tests that @noimplement is a valid tag on an interface in the 
	 * the testing package a.b.c using an incremental build
	 */
	public void testValidInterfaceTag1I() {
		deployIncrementalBuildTagTest(TESTING_PACKAGE, "test1", false);
	}

	/**
	 * Tests that @noimplement is a valid tag on an interface in the 
	 * the testing package a.b.c using a full build
	 */
	public void testValidInterfaceTag1F() {
		deployFullBuildTagTest(TESTING_PACKAGE, "test1", false);
	}
	
	/**
	 * Tests that @noimplement is a valid tag on an interface in the 
	 * the testing package a.b.c using an incremental build
	 */
	public void testValidInterfaceTag2I() {
		deployIncrementalBuildTagTest(TESTING_PACKAGE, "test2", false);
	}

	/**
	 * Tests that @noimplement is a valid tag on an interface in the 
	 * the testing package a.b.c using a full build
	 */
	public void testValidInterfaceTag2F() {
		deployFullBuildTagTest(TESTING_PACKAGE, "test2", false);
	}
	
	/**
	 * Tests that @noimplement is a valid tag on an interface in the 
	 * the testing package a.b.c using an incremental build
	 */
	public void testValidInterfaceTag3I() {
		deployIncrementalBuildTagTest(TESTING_PACKAGE, "test3", false);
	}

	/**
	 * Tests that @noimplement is a valid tag on an interface in the 
	 * the testing package a.b.c using a full build
	 */
	public void testValidInterfaceTag3F() {
		deployFullBuildTagTest(TESTING_PACKAGE, "test3", false);
	}
	
	/**
	 * Tests that @noimplement is a valid tag on a variety of inner / outer / top-level interfaces in the 
	 * the testing package a.b.c using an incremental build
	 */
	public void testValidInterfaceTag4I() {
		deployIncrementalBuildTagTest(TESTING_PACKAGE, "test4", false);
	}

	/**
	 * Tests that @noimplement is a valid tag on a variety of inner / outer / top-level interfaces in the 
	 * the testing package a.b.c using a full build
	 */
	public void testValidInterfaceTag4F() {
		deployFullBuildTagTest(TESTING_PACKAGE, "test4", false);
	}
	
	/**
	 * Tests that @noimplement is a valid tag on an interface in the 
	 * the testing package a.b.c using an incremental build
	 */
	public void testValidInterfaceTag5I() {
		deployIncrementalBuildTagTest(TESTING_PACKAGE, "test5", false);
	}

	/**
	 * Tests that @noimplement is a valid tag on an interface in the 
	 * the testing package a.b.c using a full build
	 */
	public void testValidInterfaceTag5F() {
		deployFullBuildTagTest(TESTING_PACKAGE, "test5", false);
	}
}
