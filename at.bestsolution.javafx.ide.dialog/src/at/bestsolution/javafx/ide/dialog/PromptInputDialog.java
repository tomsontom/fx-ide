package at.bestsolution.javafx.ide.dialog;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Window;

public class PromptInputDialog extends Dialog {
	private String text;
	private TextField f;
	private String input;
	
	public PromptInputDialog(Window parent, String title, String text) {
		super(parent, title);
		this.text = text;
	}

	@Override
	protected Node createDialogArea() {
		HBox box = new HBox(5);
		box.getChildren().add(new Label(text));
		
		f = new TextField();
		box.getChildren().add(f);
		
		return box;
	}
	
	@Override
	protected void okPressed() {
		super.okPressed();
		input = f.getText();
	}
	
	public String getInput() {
		return input;
	}
}