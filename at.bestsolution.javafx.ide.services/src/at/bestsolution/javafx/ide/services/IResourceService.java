package at.bestsolution.javafx.ide.services;

import javafx.stage.Stage;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

public interface IResourceService {
	public String getIconUri();
	public String getLabel();
	public IResource create(IProject project, Stage parent);
}
