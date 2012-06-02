package at.bestsolution.javafx.ide.editor.orion.editor;

import at.bestsolution.javafx.ide.editor.orion.textview.TextView;
import javafx.util.Callback;

public interface Editor {
	public boolean isDirty();
	public void setAction(String action, Runnable actionRunnable);
	public void setInput(String title, String message, String contents);
	public void addEventListener(String type, Callback<? extends Object, Void> callback);
	public TextView getTextView();
}
