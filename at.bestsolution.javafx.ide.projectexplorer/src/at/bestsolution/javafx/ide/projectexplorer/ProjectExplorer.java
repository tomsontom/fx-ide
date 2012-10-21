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
package at.bestsolution.javafx.ide.projectexplorer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

import javax.inject.Inject;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import at.bestsolution.javafx.ide.services.FileInput;
import at.bestsolution.javafx.ide.services.IResourceService;
import at.bestsolution.javafx.ide.services.IWorkbench;

@SuppressWarnings("restriction")
public class ProjectExplorer {
	private IResourceChangeListener listener = new IResourceChangeListener() {

		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
				try {
					event.getDelta().accept(new IResourceDeltaVisitor() {

						@Override
						public boolean visit(IResourceDelta delta)
								throws CoreException {
							if (delta.getKind() == IResourceDelta.ADDED) {
								handleChange(delta.getResource(), delta
										.getResource().getParent(), true);
							} else if (delta.getKind() == IResourceDelta.REMOVED) {
								handleChange(delta.getResource(), delta
										.getResource().getParent(), false);
							}
							return true;
						}
						
						private void handleChange(final IResource resource,
								final IContainer parent, final boolean added) {
							if( resource.isDerived()  || resource.getName().startsWith(".") ) {
								return;
							}
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									TreeItem<IResource> i = map.get(parent);
									if( i != null ) {
										if( added ) {
											i.getChildren().add(new LazyTreeItem(resource));
										} else {
											Iterator<TreeItem<IResource>> it = i.getChildren().iterator();
											while( it.hasNext() ) {
												if( it.next().getValue() == resource ) {
													it.remove();
													break;
												}
											}
										}
									}
								}
							});
							
						}
					});
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
//			view.setRoot(new LazyTreeItem(workspace.getRoot()));
		}
		
	};
	
	private TreeView<IResource> view;
	
	@Inject
	IEclipseContext context;
	
	@Inject
	IWorkbench workbench;
	
	@Inject
	IWorkspace workspace;
	
	private Map<IResource, TreeItem<IResource>> map = new HashMap<IResource, TreeItem<IResource>>();
	
	private Image projectImage;
	private Image packageImage;
	private Image srcFolderImage;
	private Image javaFileImage;
	
	@Inject
	public ProjectExplorer(BorderPane container, IWorkspace workspace) {
		initImages();
		view = new TreeView<IResource>(new LazyTreeItem(workspace.getRoot()));
		view.setCellFactory(new Callback<TreeView<IResource>, TreeCell<IResource>>() {
			
			@Override
			public TreeCell<IResource> call(TreeView<IResource> param) {
				TreeCell<IResource> r = new TreeCell<IResource>() {
					@Override
					protected void updateItem(IResource item, boolean empty) {
						super.updateItem(item, empty);
						
						if( empty ) {
							setText(null);
						} else {
							setText(item.getName());
							if( item instanceof IProject ) {
								setGraphic(new ImageView(projectImage));
							} else if( item instanceof IFolder ) {
								if( "src".equals(item.getName()) ) {
									setGraphic(new ImageView(srcFolderImage));	
								} else {
									setGraphic(new ImageView(packageImage));
								}
							} else if( item instanceof IFile ) {
								if( item.getName().endsWith(".java") ) {
									setGraphic(new ImageView(javaFileImage));
								}
							}
						}
					}
				}; 
				return r;
			}
		});
		view.setShowRoot(false);
		view.setOnMouseReleased(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if( event.getClickCount() == 2 ) {
					TreeItem<IResource> item = view.getSelectionModel().getSelectedItem();
					if( item != null && item.getValue() instanceof IFile ) {
						workbench.openEditor(new FileInput((IFile) item.getValue()));
					}
				}
			}
		});
		
		ContextMenu m = new ContextMenu();
		MenuItem item = new MenuItem("Run");
		item.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				run(view.getSelectionModel().getSelectedItem().getValue());
			}
		});
		try(InputStream in = getClass().getClassLoader().getResourceAsStream("/icons/16_16/run_exc.gif")) {
			item.setGraphic(new ImageView(new Image(in)));	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m.getItems().add(item);
		
		view.setContextMenu(m);
		
		container.setCenter(view);
		
		workspace.addResourceChangeListener(listener);
	}
	
	void run(IResource resource) {
		Bundle b = FrameworkUtil.getBundle(ProjectExplorer.class);
		BundleContext btx = b.getBundleContext();
		try {
			Collection<ServiceReference<IResourceService>> refs = btx.getServiceReferences(IResourceService.class, null);
			for( ServiceReference<IResourceService> r : refs ) {
				IResourceService rs = btx.getService(r);
				if( rs.handles(resource) && rs.isRunnable(resource) ) {
					rs.launch(resource);
				}
			}
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	class LazyTreeItem extends TreeItem<IResource> {
		private boolean childrenMaterialized;
		
		public LazyTreeItem(IResource resource) {
			setValue(resource);
			map.put(resource, this);
//			System.err.println("Maping: " + resource);
//			System.err.println(map);
		}
		
		@Override
		public ObservableList<TreeItem<IResource>> getChildren() {
			return super.getChildren();
		}
		
		@Override
		public boolean isLeaf() {
			if( ! childrenMaterialized ) {
				materializeChildren();
			}
			return super.getChildren().isEmpty();
		}
		
		public void forceRefresh() {
			
		}
		
		private void materializeChildren() {
			childrenMaterialized = true;
			if( getValue() instanceof IContainer ) {
				IContainer c = (IContainer) getValue();
				try {
					IResource[] resources = c.members();
					List<LazyTreeItem> children = new ArrayList<LazyTreeItem>(resources.length);
					for( IResource r : resources ) {
						if( r.isDerived() || r.getName().startsWith(".") ) {
							continue;
						}
						children.add(new LazyTreeItem(r));
					}
//FIXME
//					for( TreeItem<IResource> t : super.getChildren() ) {
//						map.remove(t.getValue());
//					}
					super.getChildren().setAll(children);
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public IResource getCurrentResource() {
		TreeItem<IResource> item = view.getSelectionModel().getSelectedItem();
		if( item != null ) {
			return item.getValue();
		}
		
		return null;
	}
	
	private void initImages() {
		try(InputStream in = getClass().getClassLoader().getResourceAsStream("/icons/16_16/prj_obj.gif")) {
			projectImage = new Image(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try(InputStream in = getClass().getClassLoader().getResourceAsStream("/icons/16_16/package_obj.gif")) {
			packageImage = new Image(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try(InputStream in = getClass().getClassLoader().getResourceAsStream("/icons/16_16/packagefolder_obj.gif")) {
			srcFolderImage = new Image(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try(InputStream in = getClass().getClassLoader().getResourceAsStream("/icons/16_16/jcu_obj.gif")) {
			javaFileImage = new Image(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
