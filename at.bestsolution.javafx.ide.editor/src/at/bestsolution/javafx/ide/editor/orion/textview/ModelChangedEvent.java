package at.bestsolution.javafx.ide.editor.orion.textview;

public interface ModelChangedEvent {
	public int getAddedCharCount();
	public int getAddedLineCount();
	public int getRemovedCharCount();
	public int getRemovedLineCount();
	public int getStart();
}
