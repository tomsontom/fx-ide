package at.bestsolution.javafx.ide.dialog;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

public abstract class TitleAreaDialog extends Dialog {
	private String title;
	private String message;
	private URL imageURI;
	
	public TitleAreaDialog(Window parent, String title, String message, URL imageURI) {
		super(parent);
		this.title = title;
		this.message = message;
		this.imageURI = imageURI;
	}
	
	@Override
	protected final Node createDialogArea() {
		BorderPane pane = new BorderPane();
		BorderPane titleArea = new BorderPane();
		
		VBox messageArea = new VBox();
		messageArea.getChildren().add(new Label(title));
		messageArea.getChildren().add(new Label(message));
		
		titleArea.setCenter(messageArea);
		try(InputStream in = imageURI.openStream();) {
			titleArea.setRight(new ImageView(new Image(in)));	 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		pane.setTop(titleArea);
		pane.setCenter(createDialogContent());
		
		return pane;
	}
	
	@Override
	protected Insets getContentInset() {
		return new Insets(0,0,0,10);
	}

	protected abstract Node createDialogContent();
}
