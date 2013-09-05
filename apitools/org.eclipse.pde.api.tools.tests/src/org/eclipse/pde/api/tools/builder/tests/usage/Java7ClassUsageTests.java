/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.api.tools.builder.tests.usage;

import java.io.File;

import junit.framework.Test;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.pde.api.tools.internal.provisional.problems.IApiProblem;
import org.eclipse.pde.api.tools.model.tests.TestSuiteHelper;

/**
 * Test class usage for Java 7 code snippets
 * 
 * @since 1.0.100
 */
public class Java7ClassUsageTests extends ClassUsageTests {

	/**
	 * Constructor
	 * @param name
	 */
	public Java7ClassUsageTests(String name) {
		super(name);
	}

	/**
	 * @return the test class for this suite
	 */
	public static Test suite() {
		return buildTestSuite(Java7ClassUsageTests.class);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.builder.tests.usage.UsageTest#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		IProject project = getEnv().getWorkspace().getRoot().getProject("usageprojectjava7"); //$NON-NLS-1$
		if (!project.exists()) {
			IPath path = TestSuiteHelper.getPluginDirectoryPath().append(TEST_SOURCE_ROOT).append("usageprojectjava7"); //$NON-NLS-1$
			File dir = path.toFile();
			assertTrue("Test data directory does not exist: " + path.toOSString(), dir.exists()); //$NON-NLS-1$
			createExistingProject(dir, true, true);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.builder.tests.ApiBuilderTest#getTestCompliance()
	 */
	@Override
	protected String getTestCompliance() {
		return JavaCore.VERSION_1_7;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.api.tools.builder.tests.usage.ClassUsageTests#getTestSourcePath()
	 */
	@Override
	protected IPath getTestSourcePath() {
		return super.getTestSourcePath().removeLastSegments(1).append("java7"); //$NON-NLS-1$
	}

	/**
	 * Tests illegal use of classes inside a string switch block
	 * (full)
	 */
	public void testStringSwitchF() {
		x1(false);
	}
	
	/**
	 * Tests illegal use of classes inside a string switch block
	 * (incremental)
	 */
	public void testStringSwitchI() {
		x1(true);
	}
	
	private void x1(boolean inc) {
		setExpectedProblemIds(new int[] {
				getProblemId(IApiProblem.ILLEGAL_INSTANTIATE, IApiProblem.NO_FLAGS),
				getProblemId(IApiProblem.ILLEGAL_INSTANTIATE, IApiProblem.NO_FLAGS),
				getProblemId(IApiProblem.ILLEGAL_INSTANTIATE, IApiProblem.NO_FLAGS)
		});
		String typename = "testCStringSwitch"; //$NON-NLS-1$
		setExpectedMessageArgs(new String[][] {
				{CLASS_NAME, typename},
				{CLASS_NAME, typename},
				{CLASS_NAME, typename}
		});
		deployUsageTest(typename, inc);
	}
	
	/**
	 * Tests illegal use of classes inside a multi catch block
	 * (full)
	 */
	public void testMultiCatchF() {
		x2(false);
	}
	
	/**
	 * Tests illegal use of classes inside a multi catch block
	 * (incremental)
	 */
	public void testMultiCatchI() {
		x2(true);
	}
	
	private void x2(boolean inc) {
		String exceptionTypeName = "ExceptionA"; //$NON-NLS-1$
		setExpectedProblemIds(new int[] {
				getProblemId(IApiProblem.ILLEGAL_INSTANTIATE, IApiProblem.NO_FLAGS),
				getProblemId(IApiProblem.ILLEGAL_INSTANTIATE, IApiProblem.NO_FLAGS)
		});
		String typename = "testCMultiCatch"; //$NON-NLS-1$
		setExpectedMessageArgs(new String[][] {
				{exceptionTypeName, typename},
				{exceptionTypeName, typename}
		});
		deployUsageTest(typename, inc);
	}
	
	/**
	 * Tests illegal use of classes inside a try with resources block
	 * (full)
	 */
	public void testTryWithF() {
		x3(false);
	}
	
	/**
	 * Tests illegal use of classes inside a try with resources block
	 * (incremental)
	 */
	public void testTryWithI() {
		x3(true);
	}
	
	private void x3(boolean inc) {
		String resourceTypeName = "TryWithResourcesClass"; //$NON-NLS-1$
		setExpectedProblemIds(new int[] {
				getProblemId(IApiProblem.ILLEGAL_INSTANTIATE, IApiProblem.NO_FLAGS),
				getProblemId(IApiProblem.ILLEGAL_INSTANTIATE, IApiProblem.NO_FLAGS)
		});
		String typename = "testCTryWith"; //$NON-NLS-1$
		setExpectedMessageArgs(new String[][] {
				{resourceTypeName, typename},
				{resourceTypeName, typename}
		});
		deployUsageTest(typename, inc);
	}
	
	/**
	 * Tests illegal use of classes instantiated with the diamond operator
	 * (full)
	 */
	public void testDiamondF() {
		x4(false);
	}
	
	/**
	 * Tests illegal use of classes instantiated with the diamond operator
	 * (incremental)
	 */
	public void testDiamondI() {
		x4(true);
	}
	
	private void x4(boolean inc) {
		String resourceTypeName = "GenericClassUsageClass<T>"; //$NON-NLS-1$
		setExpectedProblemIds(new int[] {
				getProblemId(IApiProblem.ILLEGAL_INSTANTIATE, IApiProblem.NO_FLAGS)
		});
		String typename = "testCDiamond"; //$NON-NLS-1$
		setExpectedMessageArgs(new String[][] {
				{resourceTypeName, typename}
		});
		deployUsageTest(typename, inc);
	}
}
