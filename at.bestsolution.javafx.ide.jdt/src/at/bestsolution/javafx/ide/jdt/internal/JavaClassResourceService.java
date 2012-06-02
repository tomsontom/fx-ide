package at.bestsolution.javafx.ide.jdt.internal;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import at.bestsolution.javafx.ide.dialog.Dialog;
import at.bestsolution.javafx.ide.dialog.TitleAreaDialog;
import at.bestsolution.javafx.ide.services.IResourceService;

public class JavaClassResourceService implements IResourceService {

	@Override
	public String getIconUri() {
		return "platform:/plugin/at.bestsolution.javafx.ide.jdt/icons/16_16/class_obj.gif";
	}
	
	@Override
	public String getLabel() {
		return "Class";
	}

	@Override
	public IResource create(IProject project, Stage parent) {
		TitleAreaDialog dialog = new TitleAreaDialog(parent,"New Class", "Create a new Java Class", getClass().getClassLoader().getResource("/icons/16_16/class_obj.gif")) {
			
			@Override
			protected Node createDialogContent() {
				GridPane pane = new GridPane();

				{
					Label l = new Label("Package:");
					GridPane.setColumnIndex(l,0);
					GridPane.setRowIndex(l,0);
					
					
					TextField f = new TextField();
					GridPane.setColumnIndex(f,1);
					GridPane.setRowIndex(f,0);
				}
				
				{
					Label l = new Label("Name");
					GridPane.setColumnIndex(l,0);
					GridPane.setRowIndex(l,1);
					
					
					TextField f = new TextField();
					GridPane.setColumnIndex(f,1);
					GridPane.setRowIndex(f,1);
				}
				
				return pane;
			}
		};
		
		if( dialog.open() == Dialog.OK_BUTTON ) {
			
		}
		
		// TODO Auto-generated method stub
		return null;
	}

}
