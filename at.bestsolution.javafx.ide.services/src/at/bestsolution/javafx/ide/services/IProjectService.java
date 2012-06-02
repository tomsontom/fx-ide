package at.bestsolution.javafx.ide.services;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IProgressMonitor;

public interface IProjectService {
	public String getNewProjectIconURI();
	public String getLabel();
	public void createProject(IWorkspace workspace, IProgressMonitor monitor, String projectName);
}
