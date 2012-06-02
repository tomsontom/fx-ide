package at.bestsolution.javafx.ide.projectexplorer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;

public class ProjectExplorer {
	private IResourceChangeListener listener = new IResourceChangeListener() {

		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			System.err.println("Source change");
		}
		
	};
	
	private TreeView<IResource> view;
	
	@Inject
	public ProjectExplorer(BorderPane container, IWorkspace workspace) {
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
								try(InputStream in = getClass().getClassLoader().getResourceAsStream("/icons/16_16/prj_obj.gif")) {
									setGraphic(new ImageView(new Image(in)));
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
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
					System.err.println("Opening file");
				}
			}
		});
		container.setCenter(view);
		
		workspace.addResourceChangeListener(listener);
	}
	
	static class LazyTreeItem extends TreeItem<IResource> {
		private boolean childrenMaterialized;
		
		public LazyTreeItem(IResource resource) {
			setValue(resource);
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
		
		private void materializeChildren() {
			childrenMaterialized = true;
			if( getValue() instanceof IContainer ) {
				IContainer c = (IContainer) getValue();
				try {
					IResource[] resources = c.members();
					List<LazyTreeItem> children = new ArrayList<LazyTreeItem>(resources.length);
					for( IResource r : resources ) {
						children.add(new LazyTreeItem(r));
					}
					super.getChildren().addAll(children);
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public IProject getCurrentProject() {
		IResource r = view.getSelectionModel().getSelectedItem().getValue();
		return r.getProject();
	}
}
