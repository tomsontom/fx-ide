package at.bestsolution.javafx.ide.dialog;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Window;

public class MessageDialog extends Dialog {
	
	public enum Type {
		OK,
		INFO,
		QUESTION,
		WARNING,
		ERROR
	}
	
	private Type type;
	private String message;
	
	public MessageDialog(Window parent, Type type, String title, String message) {
		super(parent, title);
		this.type = type;
		this.message = message;
	}

	private ImageView getImage() {
		switch (type) {
		case OK:
			return new ImageView(getClass().getResource("icons/dialog-ok-apply.png").toExternalForm());
		case ERROR:
			return new ImageView(getClass().getResource("icons/dialog-error.png").toExternalForm());
		case INFO:
			return new ImageView(getClass().getResource("icons/dialog-information.png").toExternalForm());
		case QUESTION:
			return new ImageView(getClass().getResource("icons/system-help.png").toExternalForm());
		case WARNING:
			return new ImageView(getClass().getResource("icons/dialog-warning.png").toExternalForm());
		default:
			break;
		}
		return null;
	}
	
	@Override
	protected Node createDialogArea() {
		Label l = new Label(message, getImage());
		return l;
	}

}
