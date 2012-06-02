package at.bestsolution.javafx.ide.services;


public interface IEditorService {
	public String getIconUri();
	public String getTitle(IEditorInput input);
	public Class<?> getEditorClass();
	public boolean handlesInput(IEditorInput input);
}
