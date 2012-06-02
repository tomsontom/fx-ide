package at.bestsolution.javafx.ide.dialog;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public abstract class Dialog {
	public static final int OK_BUTTON = 1;
	public static final int CANCEL_BUTTON = 2;
	
	private Stage stage;
	private boolean blockOnOpen = true;
	private int returnCode;
	private List<Button> buttons;
	
	public Dialog(Window parent) {
		
	}
	
	protected Parent createContents() {
		BorderPane p = new BorderPane();
		HBox box = new HBox();
		box.setPadding(getContentInset());
		
		Node content = createDialogArea();
		HBox.setHgrow(content, Priority.ALWAYS);
		box.getChildren().add(content);
		
		p.setCenter(box);
		p.setBottom(createButtonBar());
		return p;
	}
	
	protected Insets getContentInset() {
		return new Insets(10,10,0,10);
	}
	
	protected abstract Node createDialogArea();

	protected Node createButtonBar() {
		HBox b = new HBox(10);
		b.setPadding(new Insets(10));
		Region spacer = new Region();
		
		b.getChildren().add(spacer);
		HBox.setHgrow(spacer, Priority.ALWAYS);
		
		buttons = createButtonsForBar();
		b.getChildren().addAll(buttons);
		
		return b;
	}
	
	protected List<Button> createButtonsForBar() {
		List<Button> rv = new ArrayList<Button>();
		rv.add(createButtonForBar(CANCEL_BUTTON, "Cancel"));
		rv.add(createButtonForBar(OK_BUTTON, "Ok"));
		return rv;
	}
	
	protected Button createButtonForBar(final int type, String label) {
		Button b = new Button(label);
		b.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				buttonPressed(type);
			}
		});
		return b;
	}
	
	protected void buttonPressed(int type) {
		if( type == CANCEL_BUTTON ) {
			cancelPressed();
		} else if( type == OK_BUTTON ) {
			okPressed();
		}
	}
	
	protected void okPressed() {
		returnCode = OK_BUTTON;
		close();
	}
	
	protected void cancelPressed() {
		returnCode = CANCEL_BUTTON;
		close();
	}
	
	protected void close() {
		stage.close();
	}
	
	protected Stage create() {
		Stage stage = new Stage(StageStyle.UTILITY);
		Parent content = createContents();
		stage.setScene(new Scene(content));
		return stage;
	}
	
	protected Point2D getInitialSize() {
		return new Point2D(stage.getScene().getRoot().prefWidth(-1), stage.getScene().getRoot().prefHeight(-1));
	}
	
	void layout() {
		double maxWidth = 0;
		for( Button b : buttons ) {
			maxWidth = Math.max(maxWidth, b.prefWidth(-1));
		}
		
		for( Button b : buttons ) {
			b.setPrefWidth(maxWidth);
		}
		
//		Point2D size = getInitialSize();
//		stage.setWidth(size.getX());
//		stage.setHeight(size.getY());
//		stage.sizeToScene();
	}
	
	public int open() {
		if( stage == null ) {
			stage = create();
			stage.showingProperty().addListener(new ChangeListener<Boolean>() {

				@Override
				public void changed(
						ObservableValue<? extends Boolean> observable,
						Boolean oldValue, Boolean newValue) {
					if( newValue ) {
						layout();
					}
				}
			});
		}
		
		if( blockOnOpen ) {
			stage.showAndWait();			
		} else {
			stage.show();
		}
		
		return returnCode;
	}
}
