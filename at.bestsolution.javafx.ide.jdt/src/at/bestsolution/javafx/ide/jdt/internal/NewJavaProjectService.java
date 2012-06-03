package at.bestsolution.javafx.ide.jdt.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.eclipse.m2e.core.project.ResolverConfiguration;

import at.bestsolution.javafx.ide.jdt.internal.jdt.BuildPathSupport;
import at.bestsolution.javafx.ide.jdt.internal.jdt.BuildPathsBlock;
import at.bestsolution.javafx.ide.jdt.internal.jdt.CPListElement;
import at.bestsolution.javafx.ide.jdt.internal.jdt.CoreUtility;
import at.bestsolution.javafx.ide.jdt.internal.jdt.JavaModelUtil;
import at.bestsolution.javafx.ide.services.IProjectService;

public class NewJavaProjectService implements IProjectService {

	@Override
	public String getNewProjectIconURI() {
		return "platform:/plugin/at.bestsolution.javafx.ide.jdt/icons/16_16/newjprj_wiz.gif";
	}

	@Override
	public String getLabel() {
		return "Java Project";
	}

	@Override
	public void createProject(IWorkspace workspace, IProgressMonitor monitor,
			final String projectName) {
		final IProject project = workspace.getRoot().getProject(projectName);
		final IProjectDescription pd = workspace
				.newProjectDescription(projectName);
		
		try {
			workspace.run(new IWorkspaceRunnable() { 

				public void run(IProgressMonitor monitor) throws CoreException {
					if (!project.exists()) {
						project.create(pd, monitor);
					}
					if (!project.isOpen()) {
						project.open(monitor);
					}
					
					IJavaProject javaProject = JavaCore.create(project);
					
					
					configureProject(project, monitor);
					
					IFolder srcFolder = project.getFolder(new Path("src"));
					
					List<CPListElement> classPathEntries = new ArrayList<CPListElement>();
					CPListElement jreContainer = new CPListElement(javaProject, IClasspathEntry.CPE_CONTAINER, new Path(JavaRuntime.JRE_CONTAINER), null);
					CPListElement src = new CPListElement(javaProject, IClasspathEntry.CPE_SOURCE, srcFolder.getFullPath(), srcFolder);
					classPathEntries.add(jreContainer);
					classPathEntries.add(src);
					
//					for( IExecutionEnvironment e : JavaRuntime.getExecutionEnvironmentsManager().getExecutionEnvironments() ) {
//						System.err.println(e.getId());
//					}
//					
//					JavaRuntime.newJREContainerPath(environment)
					
					flush(classPathEntries, project.getFolder(new Path("bin")).getFullPath(), javaProject, null, monitor);
					
					
					
//					addMaven(project);
				}
				
			},monitor);
		} catch (CoreException e) {
			//TODO Need to log
			e.printStackTrace();
		}
	}
	
	private void addMaven(IProject prj) throws CoreException {
		ResolverConfiguration configuration = new ResolverConfiguration();
		configuration.setResolveWorkspaceProjects(false);
//		configuration.setSelectedProfiles(""); //$NON-NLS-1$

		boolean hasMavenNature = prj.hasNature(IMavenConstants.NATURE_ID);

		IProjectConfigurationManager configurationManager = MavenPlugin.getProjectConfigurationManager();

		configurationManager.enableMavenNature(prj, configuration, new NullProgressMonitor());

		if (!hasMavenNature) {
			configurationManager.updateProjectConfiguration(prj, new NullProgressMonitor());
		}
	}
	
	private void configureProject(IProject project, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] prevNatures= description.getNatureIds();
		String[] newNatures= new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length]= JavaCore.NATURE_ID;
		description.setNatureIds(newNatures);
		project.setDescription(description, monitor);
	}
	
	public static void flush(List<CPListElement> classPathEntries, IPath outputLocation, IJavaProject javaProject, String newProjectCompliance, IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		IProject project = javaProject.getProject();
		IPath projPath= project.getFullPath();
		
		IPath oldOutputLocation;
		try {
			oldOutputLocation= javaProject.getOutputLocation();
		} catch (CoreException e) {
			oldOutputLocation= projPath.append("bin"/*PreferenceConstants.getPreferenceStore().getString(PreferenceConstants.SRCBIN_BINNAME)*/);
		}
		
		if (oldOutputLocation.equals(projPath) && !outputLocation.equals(projPath)) {
			if (BuildPathsBlock.hasClassfiles(project)) {
//				if (BuildPathsBlock.getRemoveOldBinariesQuery(JavaPlugin.getActiveWorkbenchShell()).doQuery(false, projPath)) {
					BuildPathsBlock.removeOldClassfiles(project);
//				}
			}
		} else if (!outputLocation.equals(oldOutputLocation)) {
			IFolder folder= ResourcesPlugin.getWorkspace().getRoot().getFolder(oldOutputLocation);
			if (folder.exists()) {
				if (folder.members().length == 0) {
					BuildPathsBlock.removeOldClassfiles(folder);
				} else {
//					if (BuildPathsBlock.getRemoveOldBinariesQuery(JavaPlugin.getActiveWorkbenchShell()).doQuery(folder.isDerived(), oldOutputLocation)) {
						BuildPathsBlock.removeOldClassfiles(folder);
//					}
				}
			}
		}
		
		monitor.worked(1);

		IWorkspaceRoot fWorkspaceRoot= ResourcesPlugin.getWorkspace().getRoot();

		//create and set the output path first
		if (!fWorkspaceRoot.exists(outputLocation)) {
			IFolder folder= fWorkspaceRoot.getFolder(outputLocation);
			CoreUtility.createDerivedFolder(folder, true, true, new SubProgressMonitor(monitor, 1));
		} else {
			monitor.worked(1);
		}
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		
		int nEntries= classPathEntries.size();
		IClasspathEntry[] classpath= new IClasspathEntry[nEntries];
		int i= 0;
		
		for (Iterator<CPListElement> iter= classPathEntries.iterator(); iter.hasNext();) {
			CPListElement entry= iter.next();
			classpath[i]= entry.getClasspathEntry();
			i++;

			IResource res= entry.getResource();
			//1 tick
			if (res instanceof IFolder && entry.getLinkTarget() == null && !res.exists()) {
				CoreUtility.createFolder((IFolder)res, true, true, new SubProgressMonitor(monitor, 1));
			} else {
				monitor.worked(1);
			}

			//3 ticks
			if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				IPath folderOutput= (IPath) entry.getAttribute(CPListElement.OUTPUT);
				if (folderOutput != null && folderOutput.segmentCount() > 1) {
					IFolder folder= fWorkspaceRoot.getFolder(folderOutput);
					CoreUtility.createDerivedFolder(folder, true, true, new SubProgressMonitor(monitor, 1));
				} else {
					monitor.worked(1);
				}

				IPath path= entry.getPath();
				if (projPath.equals(path)) {
					monitor.worked(2);
					continue;
				}

				if (projPath.isPrefixOf(path)) {
					path= path.removeFirstSegments(projPath.segmentCount());
				}
				IFolder folder= project.getFolder(path);
				IPath orginalPath= entry.getOrginalPath();
				if (orginalPath == null) {
					if (!folder.exists()) {
						//New source folder needs to be created
						if (entry.getLinkTarget() == null) {
							CoreUtility.createFolder(folder, true, true, new SubProgressMonitor(monitor, 2));
						} else {
							folder.createLink(entry.getLinkTarget(), IResource.ALLOW_MISSING_LOCAL, new SubProgressMonitor(monitor, 2));
						}
					}
				} else {
					if (projPath.isPrefixOf(orginalPath)) {
						orginalPath= orginalPath.removeFirstSegments(projPath.segmentCount());
					}
					IFolder orginalFolder= project.getFolder(orginalPath);
					if (entry.getLinkTarget() == null) {
						if (!folder.exists()) {
							//Source folder was edited, move to new location
							IPath parentPath= entry.getPath().removeLastSegments(1);
							if (projPath.isPrefixOf(parentPath)) {
								parentPath= parentPath.removeFirstSegments(projPath.segmentCount());
							}
							if (parentPath.segmentCount() > 0) {
								IFolder parentFolder= project.getFolder(parentPath);
								if (!parentFolder.exists()) {
									CoreUtility.createFolder(parentFolder, true, true, new SubProgressMonitor(monitor, 1));
								} else {
									monitor.worked(1);
								}
							} else {
								monitor.worked(1);
							}
							orginalFolder.move(entry.getPath(), true, true, new SubProgressMonitor(monitor, 1));
						}
					} else {
						if (!folder.exists() || !entry.getLinkTarget().equals(entry.getOrginalLinkTarget())) {
							orginalFolder.delete(true, new SubProgressMonitor(monitor, 1));
							folder.createLink(entry.getLinkTarget(), IResource.ALLOW_MISSING_LOCAL, new SubProgressMonitor(monitor, 1));
						}
					}
				}
			} else {
				if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
					IPath path= entry.getPath();
					if (! path.equals(entry.getOrginalPath())) {
						String eeID= JavaRuntime.getExecutionEnvironmentId(path);
						if (eeID != null) {
							BuildPathSupport.setEEComplianceOptions(javaProject, eeID, newProjectCompliance);
							newProjectCompliance= null; // don't set it again below
						}
					}
					if (newProjectCompliance != null) {
						Map<String, String> options= javaProject.getOptions(false);
						JavaModelUtil.setComplianceOptions(options, newProjectCompliance);
						JavaModelUtil.setDefaultClassfileOptions(options, newProjectCompliance); // complete compliance options
						javaProject.setOptions(options);
					}
				}
				monitor.worked(3);
			}
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
		}

		javaProject.setRawClasspath(classpath, outputLocation, new SubProgressMonitor(monitor, 2));
	}
}
