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
package at.bestsolution.javafx.ide.editor.app;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import at.bestsolution.javafx.ide.dialog.Dialog;
import at.bestsolution.javafx.ide.projectexplorer.ProjectExplorer;
import at.bestsolution.javafx.ide.services.FileInput;
import at.bestsolution.javafx.ide.services.IEditorInput;
import at.bestsolution.javafx.ide.services.IEditorService;
import at.bestsolution.javafx.ide.services.IProjectService;
import at.bestsolution.javafx.ide.services.IResourceService;
import at.bestsolution.javafx.ide.services.IWorkbench;

@SuppressWarnings("restriction")
public class IDEAppLauncher implements IWorkbench {
	private ProjectExplorer explorer;
	private TabPane editorArea;
	private Stage stage;
	
	@Inject
	IEclipseContext rootContext;
	
	void initContext() {
		rootContext.set(IWorkspace.class, ResourcesPlugin.getWorkspace());
		rootContext.set(IWorkbench.class, this);
	}
	
	@PostConstruct
	void run(Stage primaryStage) {
		initContext();
		
		stage = primaryStage;
		BorderPane p = new BorderPane();
		p.setTop(createToolbar());
		
		SplitPane split = new SplitPane();
		split.getItems().add(createProjectExplorer());
		split.getItems().add(createEditorArea());
		split.setDividerPositions(0.2);
		
		p.setCenter(split);
		
		primaryStage.setScene(new Scene(p,800,600));
		primaryStage.setTitle("Bestsolution.at - JavaFX IDE");
		primaryStage.show();
	}
	
	private Node createToolbar() {
		ToolBar bar = new ToolBar();
		{
			SplitMenuButton b = new SplitMenuButton();
			
			try(InputStream in = getClass().getClassLoader().getResourceAsStream("/icons/16_16/newprj_wiz.gif");) {
				b.setGraphic(new ImageView(new Image(in)));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			bar.getItems().add(b);
			
			try {
				BundleContext btx = Activator.getContext();
				Collection<ServiceReference<IProjectService>> refs = btx.getServiceReferences(IProjectService.class, null);
				for( ServiceReference<IProjectService> r : refs) {
					final IProjectService p = btx.getService(r);
					MenuItem m = new MenuItem(p.getLabel());
					if( p.getNewProjectIconURI() != null ) {
						try {
							URL url = FileLocator.find(new URL(p.getNewProjectIconURI()));
							if( url != null ) {
								try(InputStream in = url.openStream();) {
									m.setGraphic(new ImageView(new Image(in)));
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					m.setOnAction(new EventHandler<ActionEvent>() {
						
						@Override
						public void handle(ActionEvent event) {
							handleNewProject(p);
						}
					});
					b.getItems().add(m);
				}
			} catch (InvalidSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
		try {
			SplitMenuButton b = new SplitMenuButton();
			
			try(InputStream in = getClass().getClassLoader().getResourceAsStream("/icons/16_16/newfile_wiz.gif");) {
				b.setGraphic(new ImageView(new Image(in)));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			bar.getItems().add(b);
			
			BundleContext btx = Activator.getContext();
			Collection<ServiceReference<IResourceService>> refs = btx.getServiceReferences(IResourceService.class, null);
			for( ServiceReference<IResourceService> r : refs) {
				final IResourceService resourceService = btx.getService(r);
				MenuItem m = new MenuItem(resourceService.getLabel());
				if( resourceService.getIconUri() != null ) {
					try {
						URL url = FileLocator.find(new URL(resourceService.getIconUri()));
						if( url != null ) {
							try(InputStream in = url.openStream();) {
								m.setGraphic(new ImageView(new Image(in)));
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				m.setOnAction(new EventHandler<ActionEvent>() {
					
					@Override
					public void handle(ActionEvent event) {
						handleNewResource(resourceService);
					}
				});
				b.getItems().add(m);
			}
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return bar;
	}
	
	void handleNewResource(final IResourceService service) {
		IResource r = explorer.getCurrentResource();
		IResource newFile;
		if( r instanceof IContainer ) {
			newFile = service.create((IContainer) r, stage);	
		} else {
			newFile = service.create(r.getParent(), stage);
		}
		
		if( newFile instanceof IFile ) {
			openEditor(new FileInput((IFile) newFile));	
		}
		
	}
	
	void handleNewProject(final IProjectService service) {
		Dialog d = new Dialog(stage,"New Project") {
			
			private TextField txt;

			@Override
			protected Node createDialogArea() {
				HBox box = new HBox(10);
				box.getChildren().add(new Label("Projectname:"));
				
				txt = new TextField();
				HBox.setHgrow(txt, Priority.ALWAYS);
				box.getChildren().add(txt);
				
				return box;
			}
			
			@Override
			protected void okPressed() {
				if( txt.getText() != null && ! txt.getText().trim().isEmpty() ) {
					service.createProject(ResourcesPlugin.getWorkspace(), new NullProgressMonitor(), txt.getText().trim());
					super.okPressed();
				}
			}
		};
		d.open();
	}
	
	private Node createProjectExplorer() {
		IEclipseContext context = rootContext.createChild("ProjectExplorer");
		BorderPane p = new BorderPane();
		context.set(BorderPane.class, p);
		
		explorer = ContextInjectionFactory.make(ProjectExplorer.class, context);
		return p;
	}
	
	private Node createEditorArea() {
		editorArea = new TabPane();
		return editorArea;
	}

	@Override
	public void openEditor(IEditorInput editorInput) {
		try {
			Bundle b = FrameworkUtil.getBundle(ProjectExplorer.class);
			BundleContext context = b.getBundleContext();
			Collection<ServiceReference<IEditorService>> refs = context.getServiceReferences(IEditorService.class, null);
			for( ServiceReference<IEditorService> r : refs ) {
				IEditorService es = context.getService(r);
				if( es.handlesInput(editorInput) ) {
					IEclipseContext editorContext = rootContext.createChild("EditorContext");
					BorderPane container = new BorderPane();
					Tab t = new Tab();
					t.setText(es.getTitle(editorInput));
					t.setContent(container);
					
					editorContext.set(BorderPane.class, container);
					editorContext.set(IEditorInput.class, editorInput);
					
					Object o = ContextInjectionFactory.make(es.getEditorClass(), editorContext);
					
					editorArea.getTabs().add(t);
					editorArea.getSelectionModel().select(t);
				}
			}
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
}
