/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package a.b.c;

import org.eclipse.pde.api.tools.annotations.NoReference;

/**
 * Tests invalid @NoReference annotation on nested inner enums
 */
public enum test1 {

	A;
	/**
	 */
	@NoReference
	enum inner {
		
	}
	
	enum inner1 {
		A;
		/**
		 */
		@NoReference
		enum inner2 {
			
		}
	}
	
	enum inner2 {
		
	}
}

enum outer {
	A;
	/**
	 */
	@NoReference
	enum inner {
		
	}
}
