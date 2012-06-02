package at.bestsolution.javafx.ide.editor;

public interface Document {
	public String get();
	public void set(String data);
	public void insert(int start, String content);
}
