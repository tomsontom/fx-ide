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
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.app.IApplicationContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import at.bestsolution.efxclipse.runtime.application.AbstractJFXApplication;
import at.bestsolution.javafx.ide.dialog.Dialog;
import at.bestsolution.javafx.ide.projectexplorer.ProjectExplorer;
import at.bestsolution.javafx.ide.services.IProjectService;
import at.bestsolution.javafx.ide.services.IResourceService;

public class Application extends AbstractJFXApplication {
	private ProjectExplorer explorer;
	private TabPane editorArea;
	private Stage stage;
	
	protected void jfxStart(IApplicationContext applicationContext, javafx.application.Application jfxApplication, Stage primaryStage) {
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
		service.create(explorer.getCurrentProject(), stage);
	}
	
	void handleNewProject(final IProjectService service) {
		Dialog d = new Dialog(stage) {
			
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
		BorderPane p = new BorderPane();
		explorer = new ProjectExplorer(p, ResourcesPlugin.getWorkspace());
		return p;
	}
	
	private Node createEditorArea() {
		editorArea = new TabPane();
		return editorArea;
	}
}