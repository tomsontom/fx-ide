package at.bestsolution.javafx.ide.services;


public interface IEditorService {
	public String getIconUri();
	public Class<?> getEditorClass();
	public boolean handlesInput(IEditorInput input);
}
