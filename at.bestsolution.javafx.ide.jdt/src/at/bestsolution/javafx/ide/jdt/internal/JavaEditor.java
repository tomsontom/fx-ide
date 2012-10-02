/*******************************************************************************
 * Copyright (c) 2012 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl<tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package at.bestsolution.javafx.ide.jdt.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.layout.BorderPane;

import javax.inject.Inject;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import at.bestsolution.javafx.ide.editor.ContentProposalComputer;
import at.bestsolution.javafx.ide.editor.Document;
import at.bestsolution.javafx.ide.editor.SourceEditor;
import at.bestsolution.javafx.ide.services.IEditorInput;
import at.bestsolution.javafx.ide.services.IResourceFileInput;

public class JavaEditor {
	private IResourceFileInput fsInput;
	private IJavaProject project;
	
	@Inject
	public JavaEditor(BorderPane pane, IEditorInput input) {
		SourceEditor editor = new SourceEditor();
		pane.setCenter(editor);

		final Document doc = createDocument(input);
		editor.setDocument(doc);
		project = JavaCore.create(fsInput.getFile().getProject());
		editor.setContentProposalComputer(new ContentProposalComputer() {

			@Override
			public List<Proposal> computeProposals(String line, String prefix,
					int offset) {
				final List<Proposal> l = new ArrayList<ContentProposalComputer.Proposal>();
				
				ICompilationUnit unit = (ICompilationUnit) JavaCore.create(fsInput.getFile());
				final IType unitType = unit.findPrimaryType();
				try {
					unit.becomeWorkingCopy(null);
					unit.getBuffer().setContents(doc.get());
					unit.codeComplete(offset, new CompletionRequestor() {
						
						@Override
						public void accept(CompletionProposal proposal) {
							String completion = new String(proposal.getCompletion());
							
//							IType ownerType = null;
//							String sType = new String(proposal.getDeclarationSignature());
//							sType = Signature.toString(sType);
//							try {
//								ownerType = project.findType(Signature.toQualifiedName(unitType.resolveType(sType)[0]));
//							} catch (Exception e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//							
//							if( ownerType == null ) {
//								return;
//							}
							
							if( ! Flags.isPublic(proposal.getFlags()) ) {
								return;
							}
							
							if( proposal.getKind() == CompletionProposal.METHOD_REF ) {
								String sig = Signature.toString(
										new String(proposal.getSignature()), 
										new String(proposal.getName()), 
										null,
										false, false);
								l.add(new Proposal(Type.METHOD, completion, 
										sig + " : " + Signature.getSimpleName(Signature.toString(Signature.getReturnType(new String(proposal.getSignature())))) + 
										" - " + Signature.getSignatureSimpleName(new String(proposal.getDeclarationSignature()))
										));
							} else if( proposal.getKind() == CompletionProposal.FIELD_REF ) {
								String description = completion + " : " + 
										(proposal.getSignature() != null ? Signature.getSignatureSimpleName(new String(proposal.getSignature())) : "<unknown>" ) + 
										" - " + 
										( proposal.getDeclarationSignature() != null ? Signature.getSignatureSimpleName(new String(proposal.getDeclarationSignature())) : "<unknown>" );
								l.add(new Proposal(Type.FIELD, completion,description));
							} else if( proposal.getKind() == CompletionProposal.TYPE_REF ) {
								if( proposal.getAccessibility() == IAccessRule.K_NON_ACCESSIBLE ) {
									return;
								}
								l.add(new Proposal(Type.TYPE, new String(proposal.getCompletion()), Signature.getSignatureSimpleName(new String(proposal.getSignature()))));
							}
						}
					});
					unit.restore();
				} catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return l;
			}

		});
		editor.setSaveCallback(new Runnable() {

			@Override
			public void run() {
				if (JavaEditor.this.fsInput != null) {
					InputStream in = new ByteArrayInputStream(doc.get()
							.getBytes());
					try {
						fsInput.getFile().setContents(in,
								IResource.FORCE | IResource.KEEP_HISTORY,
								new NullProgressMonitor());
						// fsInput.getFile().getProject().build(IncrementalProjectBuilder.FULL_BUILD,
						// new NullProgressMonitor());
						//
						// IMarker[] ms =
						// fsInput.getFile().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER,
						// true, IResource.DEPTH_INFINITE);
						// for( IMarker m : ms ) {
						// System.err.println(m);
						// }
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}

	private Document createDocument(IEditorInput input) {
		fsInput = (IResourceFileInput) input;
		try (InputStream in = fsInput.getFile().getContents();) {
			byte[] buf = new byte[1024];
			int l = 0;
			StringBuilder b = new StringBuilder();
			while ((l = in.read(buf)) != -1) {
				b.append(new String(buf, 0, l));
			}

			return new StringDocument(b);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return new StringDocument("/** Unable to extract content from '"
				+ input + "' **/");
	}

	static class StringDocument implements Document {
		private StringBuilder content;

		public StringDocument(CharSequence initialContent) {
			content = new StringBuilder(initialContent);
		}

		@Override
		public String get() {
			return content.toString();
		}

		@Override
		public void insert(int start, String data) {
			content.insert(start, data);
		}

		@Override
		public void set(String data) {
			content = new StringBuilder(data);
		}
	}
}
