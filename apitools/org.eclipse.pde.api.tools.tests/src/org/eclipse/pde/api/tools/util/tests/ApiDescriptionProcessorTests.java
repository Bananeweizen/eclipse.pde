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
package org.eclipse.pde.api.tools.util.tests;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.pde.api.tools.internal.ApiDescriptionProcessor;
import org.eclipse.pde.api.tools.internal.util.Signatures;
import org.eclipse.pde.api.tools.model.tests.TestSuiteHelper;
import org.eclipse.pde.api.tools.tests.AbstractApiTest;
import org.eclipse.pde.api.tools.ui.internal.ApiUIPlugin;
import org.eclipse.pde.api.tools.ui.internal.wizards.ApiToolingSetupRefactoring;
import org.eclipse.pde.api.tools.ui.internal.wizards.WizardMessages;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

import com.ibm.icu.text.MessageFormat;

/**
 * This class tests the {@link ApiDescriptionProcessor}
 * 
 * @since 1.0.0
 */
public class ApiDescriptionProcessorTests extends AbstractApiTest {

	/**
	 * Visitor used to inspect the 'after' class files once they have had tags
	 * added to ensure the tags as specified in the component.xml file were
	 * added.
	 */
	class ChangeVisitor extends ASTVisitor {
		String type, membername, signature, innertypename;

		String[] expectedtags = null;
		boolean processed = false;
		/**
		 * Constructor
		 * 
		 * @param type
		 * @param membername
		 * @param signature
		 */
		public ChangeVisitor(String type, String innertypename, String membername, String signature, String[] expectedtags) {
			this.type = type;
			this.membername = membername;
			this.signature = signature;
			this.expectedtags = expectedtags;
			this.innertypename = innertypename;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.FieldDeclaration)
		 */
		public boolean visit(FieldDeclaration node) {
			if (signature != null) {
				// means we are looking for a method
				return false;
			}
			List<VariableDeclarationFragment> fields = node.fragments();
			VariableDeclarationFragment fragment = null;
			for(Iterator<VariableDeclarationFragment> iter = fields.iterator(); iter.hasNext();) {
				fragment = iter.next();
				if(fragment.getName().getFullyQualifiedName().equals(membername)) {
					Javadoc docnode = node.getJavadoc();
					assertNotNull("the field: "+membername+" must have a javadoc node", docnode);
					assertTrue("the field: "+membername+" should contain all of the tags: "+ getStringValue(expectedtags), containsAllTags(docnode.tags()));
					processed = true;
				}
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodDeclaration)
		 */
		public boolean visit(MethodDeclaration node) {
			if(node.getName().getFullyQualifiedName().equals(membername)) {
				String sig = Signatures.getMethodSignatureFromNode(node);
				if(signature.equals(sig)) {
					Javadoc docnode = node.getJavadoc();
					assertNotNull("the method: "+membername+" ["+signature+"] must have a javadoc node", docnode);
					assertTrue("the method: "+membername+" ["+signature+"] should contain all of the tags: " + getStringValue(expectedtags), containsAllTags(docnode.tags()));
					processed = true;
				}
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TypeDeclaration)
		 */
		public boolean visit(TypeDeclaration node) {
			if(membername == null && signature == null) {
				String name = node.getName().getFullyQualifiedName(); 
				if((innertypename == null && name.equals(type)) || name.equals(innertypename)) {
					Javadoc docnode = node.getJavadoc();
					assertNotNull("the type: "+name+" must have a javadoc node", docnode);
					assertTrue("the type: "+name+" should contain all of the tags: " + getStringValue(expectedtags), containsAllTags(docnode.tags()));
					processed = true;
				}
			}
			return membername != null || innertypename != null;
		}
		
		/**
		 * Determines if the listing of doc tags from the declaration contains all of the expected tags
		 * @param tags
		 * @return true if the tag list contains all of the expected tags, false otherwise
		 */
		private boolean containsAllTags(List<TagElement> tags) {
			boolean allfound = true;
			TagElement element = null;
			for(int i = 0; i < expectedtags.length; i++) {
				boolean contained = false;
				for(int j = 0; j < tags.size(); j++) {
					element = tags.get(j);
					if(expectedtags[i].equals(element.getTagName())) {
						contained = true;
					}
				}
				allfound &= contained;
			}
			return allfound;
		}
		
		private String getStringValue(String[] tags) {
			StringBuffer buffer = new StringBuffer();
			for (int i = 0, max = tags.length; i < max; i++) {
				if (i > 0) {
					buffer.append(',');
				}
				buffer.append(tags[i]);
			}
			return String.valueOf(buffer);
		}
	}

	private static IPath ROOT_PATH = TestSuiteHelper.getPluginDirectoryPath().append("test-source").append("javadoc");
	private static File componentxml = new File(ROOT_PATH.append("component.xml").toOSString());
	private static IJavaProject project = null;

	/**
	 * Tests that the component.xml file is parsed if it is provided in one of
	 * the doc'd three forms:
	 * <ol>
	 * <li>a directory containing the component.xml file</li>
	 * <li>a component.xml file</li>
	 * <li>a jar file containing the component.xml file</li>
	 * </ol>
	 */
	public void testSerializeComponentXml() {
		String xml = ApiDescriptionProcessor.serializeComponentXml(new File(ROOT_PATH.toOSString()));
		assertNotNull("The component xml file must exist and be parsable from a root directory", xml);
		xml = ApiDescriptionProcessor.serializeComponentXml(componentxml);
		assertNotNull("The component xml file must exist and be parsable from a component.xml file", xml);
		xml = ApiDescriptionProcessor.serializeComponentXml(new File(ROOT_PATH.append("component.jar").toOSString()));
		assertNotNull("The component xml file must exist and be parsable from a jar file", xml);
	}

	/**
	 * Tests getting the java project to use with these tests
	 */
	public void testGetTestingProject() {
		project = getTestingJavaProject(TESTING_PROJECT_NAME);
		assertNotNull("the testing project must not be null", project);
		assertNotNull("the testing project must exist", project.exists());
	}

	/**
	 * Tests the actual updating process, it should not fail
	 */
	public void testProcessUpdate() {
		try {
			ApiToolingSetupRefactoring refactoring = new ApiToolingSetupRefactoring();
			CompositeChange change = new CompositeChange("Test");
			createTagChanges(change, project, componentxml);
			refactoring.addChange(change);
			performRefactoring(refactoring);
		}
		catch (CoreException e) {
			fail(e.getMessage());
		} 
	}
	
	/**
	 * Creates all of the text edit changes collected from the processor. The collected edits are arranged as multi-edits 
	 * for the one file that they belong to
	 * @param projectchange
	 * @param project
	 * @param cxml
	 */
	private void createTagChanges(CompositeChange projectchange, IJavaProject project, File cxml) {
		try {
			HashMap<IFile, Set<TextEdit>> map = new HashMap<IFile, Set<TextEdit>>();
			ApiDescriptionProcessor.collectTagUpdates(project, cxml, map);
			IFile file = null;
			TextFileChange change = null;
			MultiTextEdit multiedit = null;
			Set<TextEdit> alledits = null;
			TextEdit edit = null;
			for(Iterator<IFile> iter = map.keySet().iterator(); iter.hasNext();) {
				file = iter.next();
				change = new TextFileChange(MessageFormat.format(WizardMessages.JavadocTagRefactoring_2, new String[] {file.getName()}), file);
				multiedit = new MultiTextEdit();
				change.setEdit(multiedit);
				alledits = map.get(file);
				if(alledits != null) {
					for(Iterator<TextEdit> iter2 = alledits.iterator(); iter2.hasNext();) {
						edit = (TextEdit) iter2.next();
						multiedit.addChild(edit);
					}
				}
				if(change != null) {
					projectchange.add(change);
				}
			}
		}
		catch (CoreException e) {
			ApiUIPlugin.log(e);
		} 
		catch (IOException e) {
			ApiUIPlugin.log(e);
		}
	}
	
	/**
	 * Processes the change from original to updated
	 * 
	 * @param typename the name of the type to query
	 * @param membername the name of the member
	 * @param signature the signature of the member
	 * @param the tags we expect to see
	 */
	protected void processUpdatedItem(String typename, String innertypename, String membername, String signature, String[] expectedtags) {
		try {
			IType type = project.findType("javadoc", typename);
			assertNotNull("the type for javadoc." + typename + " must exist", type);
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setSource(type.getCompilationUnit());
			CompilationUnit cunit = (CompilationUnit) parser.createAST(new NullProgressMonitor());
			ChangeVisitor visitor = new ChangeVisitor(typename, innertypename, membername, signature, expectedtags); 
			cunit.accept(visitor);
			assertTrue("the specified node should have been processed", visitor.processed);
		} catch (JavaModelException jme) {
			fail("the test class javadoc." + typename + " had problems loading");
		}
	}

	/**
	 * Tests the addition of a javadoc tag to a class. Uses
	 * <code>JavadocTestClass1</code>
	 */
	public void testProcessClassAddition() {
		processUpdatedItem("JavadocTestClass1", null, null, null, new String[] {"@noinstantiate"});
	}

	/**
	 * Tests the addition of a javadoc tag to a class that does not have a
	 * javadoc section yet Uses <code>JavadocTestClass7</code>
	 */
	public void testProcessClassAdditionNoDocElement() {
		processUpdatedItem("JavadocTestClass7", null, null, null, new String[] {"@noextend", "@noinstantiate"});
	}

	/**
	 * Tests the addition of a javadoc tag to a method that does not have a
	 * javadoc section yet Uses <code>JavadocTestClass7</code>
	 */
	public void testProcessMethodAdditionNoDocElement() {
		processUpdatedItem("JavadocTestClass7", null, "m1", "()V", new String[] {"@nooverride"});
	}

	/**
	 * Tests the addition of a javadoc tag to a field that does not have a
	 * javadoc section yet Uses <code>JavadocTestClass7</code>
	 */
	public void testProcessFieldAdditionNoDocElement() {
		processUpdatedItem("JavadocTestClass7", null, "f1", null, new String[] {"@noreference"});
	}
	
	/**
	 * Tests the addition of a javadoc tag to an inner class. Uses
	 * <code>JavadocTestClass2</code>
	 */
	public void testProcessInnerClassAddition() {
		processUpdatedItem("JavadocTestClass2", "Inner", null, null, new String[] {"@noinstantiate"});
	}

	/**
	 * Tests the addition of a javadoc tags to methods. Uses
	 * <code>JavadocTestClass3</code>
	 */
	public void testProcessMethodAddition() {
		processUpdatedItem("JavadocTestClass3", null, "m1", "()V", new String[] {"@nooverride"});
		processUpdatedItem("JavadocTestClass3", null, "m2", "()V", new String[] {"@noreference"});
	}

	/**
	 * Tests the addition of a javadoc tags to fields. Uses
	 * <code>JavadocTestClass4</code>
	 */
	public void testProcessFieldAddition() {
		processUpdatedItem("JavadocTestClass4", null, "f1", null, new String[] {"@noreference"});
		processUpdatedItem("JavadocTestClass4", null, "f2", null, new String[] {"@noreference"});
	}

	/**
	 * Tests the addition of a javadoc tags to methods in inner classes. Uses
	 * <code>JavadocTestClass6</code>
	 */
	public void testProcessInnerMethodAddition() {
		processUpdatedItem("JavadocTestClass6", "Inner2", "m1", "()V", new String[] {"@nooverride"});
		processUpdatedItem("JavadocTestClass6", "Inner2", "m2", "()V", new String[] {"@noreference"});
	}

	/**
	 * Tests the addition of a javadoc tags to fields in inner classes. Uses
	 * <code>JavadocTestClass5</code>
	 */
	public void testProcessInnerFieldAddition() {
		processUpdatedItem("JavadocTestClass5", "Inner2", "f1", null, new String[] {"@noreference"});
		processUpdatedItem("JavadocTestClass5", "Inner2", "f2", null, new String[] {"@noreference"});
	}
	
	/**
	 * Tests that the old 'subclass' attribute is correctly resolved to 'noextend' restriction for tag updating.
	 * Tests the case of bug 210786 (https://bugs.eclipse.org/bugs/show_bug.cgi?id=210786)
	 * Uses <code>JavadocTestClass8</code> 
	 */
	public void testProcessSubclassAttribute() {
		processUpdatedItem("JavadocTestClass8", null, null, null, new String[] {"@noextend"});
	}
}
