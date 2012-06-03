package at.bestsolution.javafx.ide.services;

import javafx.stage.Stage;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;

public interface IResourceService {
	public String getIconUri();
	public String getLabel();
	public IResource create(IContainer container, Stage parent);
	public boolean handles(IResource resource);
	public boolean isRunnable(IResource resource);
	public void launch(IResource resource);
}
