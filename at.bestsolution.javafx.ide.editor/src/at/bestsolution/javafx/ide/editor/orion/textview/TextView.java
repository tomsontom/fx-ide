package at.bestsolution.javafx.ide.editor.orion.textview;

import javafx.util.Callback;

public interface TextView {
	public void addEventListener(String type, Callback<? extends Object, Void> callback);
	public String getText();
	public String getText(int start, int end);
}
