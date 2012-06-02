package at.bestsolution.javafx.ide.services;

import org.eclipse.core.resources.IFile;

public interface IResourceFileInput extends IEditorInput {
	public IFile getFile();
}
