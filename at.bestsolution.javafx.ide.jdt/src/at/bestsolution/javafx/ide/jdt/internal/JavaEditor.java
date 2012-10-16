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

import java.util.ArrayList;
import java.util.List;

import javafx.scene.layout.BorderPane;

import javax.inject.Inject;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import at.bestsolution.javafx.ide.editor.ContentProposalComputer;
import at.bestsolution.javafx.ide.editor.Document;
import at.bestsolution.javafx.ide.editor.SourceEditor;
import at.bestsolution.javafx.ide.services.IEditorInput;
import at.bestsolution.javafx.ide.services.IResourceFileInput;

public class JavaEditor {
	private ICompilationUnit unit;
	
	@Inject
	public JavaEditor(BorderPane pane, IEditorInput input) {
		SourceEditor editor = new SourceEditor();
		pane.setCenter(editor);

		IResourceFileInput fsInput = (IResourceFileInput) input;
		unit = (ICompilationUnit) JavaCore.create(fsInput.getFile());
		try {
			unit.becomeWorkingCopy(null);
		} catch (JavaModelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		final Document doc = createDocument(unit);
		editor.setDocument(doc);
		editor.setContentProposalComputer(new ContentProposalComputer() {

			@Override
			public List<Proposal> computeProposals(String line, String prefix,
					int offset) {
				final List<Proposal> l = new ArrayList<ContentProposalComputer.Proposal>();
				
				try {
					unit.codeComplete(offset, new CompletionRequestor() {
						
						@Override
						public void accept(CompletionProposal proposal) {
							String completion = new String(proposal.getCompletion());
							
							if( ! Flags.isPublic(proposal.getFlags()) ) {
								return;
							}
							
							if( proposal.getKind() == CompletionProposal.METHOD_REF ) {
								String sig = Signature.toString(
										new String(proposal.getSignature()), 
										new String(proposal.getName()), 
										null,
										false, false);
								StyledString s = new StyledString(sig + " : " + Signature.getSimpleName(Signature.toString(Signature.getReturnType(new String(proposal.getSignature())))));
								s.appendString(" - " + Signature.getSignatureSimpleName(new String(proposal.getDeclarationSignature())), Style.colored("#AAAAAA"));
								
								l.add(new Proposal(Type.METHOD, completion, s));
							} else if( proposal.getKind() == CompletionProposal.FIELD_REF ) {
								StyledString s = new StyledString(completion + " : " + (proposal.getSignature() != null ? Signature.getSignatureSimpleName(new String(proposal.getSignature())) : "<unknown>" ));
								s.appendString(" - " +  (proposal.getDeclarationSignature() != null ? Signature.getSignatureSimpleName(new String(proposal.getDeclarationSignature())) : "<unknown>"), Style.colored("#AAAAAA"));
								l.add(new Proposal(Type.FIELD, completion,s));
							} else if( proposal.getKind() == CompletionProposal.TYPE_REF ) {
								if( proposal.getAccessibility() == IAccessRule.K_NON_ACCESSIBLE ) {
									return;
								}
								
								StyledString s = new StyledString(Signature.getSignatureSimpleName(new String(proposal.getSignature())));
								s.appendString(" - " + new String(proposal.getDeclarationSignature()), Style.colored("#AAAAAA"));
								l.add(new Proposal(Type.TYPE, new String(proposal.getCompletion()), s));
							} else {
								System.err.println(proposal);
							}
						}
					});
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
				try {
					unit.commitWorkingCopy(true, null);
				} catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	private Document createDocument(ICompilationUnit unit) {
		return new CompilationUnitDocument(unit);
	}

	static class CompilationUnitDocument implements Document {
		private ICompilationUnit unit;

		public CompilationUnitDocument(ICompilationUnit unit) {
			this.unit = unit;
		}

		@Override
		public String get() {
			try {
				return unit.getBuffer().getContents();
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "";
		}

		@Override
		public void insert(int start, String data) {
			try {
				unit.getBuffer().replace(start, 0, data);
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void set(String data) {
			try {
				unit.getBuffer().setContents(data);
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
