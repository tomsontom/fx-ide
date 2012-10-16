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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;

import at.bestsolution.javafx.ide.editor.ContentProposalComputer;
import at.bestsolution.javafx.ide.editor.Document;
import at.bestsolution.javafx.ide.editor.ProblemMarker;
import at.bestsolution.javafx.ide.editor.SourceEditor;
import at.bestsolution.javafx.ide.services.IEditorInput;
import at.bestsolution.javafx.ide.services.IResourceFileInput;

public class JavaEditor {
	private ICompilationUnit unit;
	private SourceEditor editor;
	
	@SuppressWarnings("deprecation")
	@Inject
	public JavaEditor(BorderPane pane, IEditorInput input) {
		editor = new SourceEditor();
		pane.setCenter(editor);

		IResourceFileInput fsInput = (IResourceFileInput) input;
		unit = (ICompilationUnit) JavaCore.create(fsInput.getFile());
//		unit.getResource().getWorkspace().addResourceChangeListener(new IResourceChangeListener() {
//			
//			@Override
//			public void resourceChanged(IResourceChangeEvent event) {
//				if( event.getType() == IResourceChangeEvent.POST_BUILD ) {
//					for( IMarkerDelta d : event.findMarkerDeltas("org.eclipse.core.resources.problemmarker", true) ) {
//						System.err.println(d);
//					}
//				}
//			}
//		});
		
		try {
			unit.becomeWorkingCopy(new IProblemRequestor() {
				private List<ProblemMarker> l = new ArrayList<>();
				private boolean running;
				private CompilationUnit c;
				
				@Override
				public boolean isActive() {
					return ! running;
				}
				
				@Override
				public void endReporting() {
					running = false;
					setMarkers(l);
				}
				
				@Override
				public void beginReporting() {
					running = true;
					c = null; 
					try {
						 c = unit.reconcile(AST.JLS4, true, null, null);
					} catch (JavaModelException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					l.clear();
				}
				
				@Override
				public void acceptProblem(IProblem problem) {
					int linenumber = problem.getSourceLineNumber();
					int startCol = problem.getSourceStart();
					int endCol = problem.getSourceEnd();
					
					if( c != null ) {
						startCol = c.getColumnNumber(startCol);
						endCol = c.getColumnNumber(endCol) ;
						if( endCol == startCol ) {
							endCol++;
						}
					}
					
					String description = problem.getMessage();
					ProblemMarker marker = new ProblemMarker( problem.isError() ? at.bestsolution.javafx.ide.editor.ProblemMarker.Type.ERROR : at.bestsolution.javafx.ide.editor.ProblemMarker.Type.WARNING, linenumber, startCol, endCol, description);
					l.add(marker);
				}
			}, null);
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
		
		calculateInitialMarkers();
	}
	
	void calculateInitialMarkers() {
		try {
			List<ProblemMarker> list = new ArrayList<>();
			
			CompilationUnit c = null;
			try {
				 c = unit.reconcile(AST.JLS4, true, null, null);
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for( IMarker m : unit.getResource().findMarkers("org.eclipse.core.resources.problemmarker", true, IResource.DEPTH_INFINITE) ) {
				System.err.println("INITIAL ONES");
				int linenumber = ((Number)m.getAttribute("lineNumber")).intValue();
				int startCol = ((Number)m.getAttribute("charStart")).intValue();
				int endCol = ((Number)m.getAttribute("charEnd")).intValue();
				String description = m.getAttribute("message").toString();
				int severity = ((Number)m.getAttribute("severity")).intValue();
				
				if( c != null ) {
					startCol = c.getColumnNumber(startCol);
					endCol = c.getColumnNumber(endCol);
				}
				
				if( severity == IMarker.SEVERITY_ERROR || severity == IMarker.SEVERITY_WARNING ) {
					ProblemMarker marker = new ProblemMarker( severity == IMarker.SEVERITY_ERROR ? at.bestsolution.javafx.ide.editor.ProblemMarker.Type.ERROR : at.bestsolution.javafx.ide.editor.ProblemMarker.Type.WARNING, linenumber, startCol, endCol, description);
					list.add(marker);
				}
			}
			
			setMarkers(list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void setMarkers(List<ProblemMarker> markers) {
		editor.setProblemMarkers(markers);
	}

	private Document createDocument(ICompilationUnit unit) {
		return new CompilationUnitDocument(this, unit);
	}

	static class CompilationUnitDocument implements Document {
		private final ICompilationUnit unit;
		private final JavaEditor editor;

		public CompilationUnitDocument(JavaEditor editor, ICompilationUnit unit) {
			this.unit = unit;
			this.editor = editor;
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
				unit.reconcile(ICompilationUnit.NO_AST, true, null, null);
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void set(String data) {
			try {
				unit.getBuffer().setContents(data);
				unit.reconcile(ICompilationUnit.NO_AST, true, null, null);
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
}
