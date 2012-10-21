/*******************************************************************************
 * Copyright (c) 2012 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl<tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package at.bestsolution.javafx.ide.jdt.internal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JProgressBar;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;

import at.bestsolution.efxclipse.runtime.dialogs.TitleAreaDialog;
import at.bestsolution.javafx.ide.services.IResourceService;

public class JavaClassResourceService implements IResourceService {
	private IResource resource;

	@Override
	public String getIconUri() {
		return "platform:/plugin/at.bestsolution.javafx.ide.jdt/icons/16_16/class_obj.gif";
	}

	@Override
	public String getLabel() {
		return "Class";
	}

	@Override
	public IResource create(final IContainer container, Stage parent) {
		resource = null;
		
		TitleAreaDialog dialog = new TitleAreaDialog(parent, "New Class", "New Class",
				"Create a new Java Class", getClass().getClassLoader()
						.getResource("/icons/wizban/newclass_wiz.png")) {

			private TextField packageName;
			private TextField className;

			@Override
			protected Node createDialogContent() {
				GridPane pane = new GridPane();
				pane.setHgap(10);
				pane.setVgap(5);

				{
					Label l = new Label("Package:");
					pane.add(l, 0, 0);

					packageName = new TextField();
					pane.add(packageName, 1, 0);
					GridPane.setHgrow(packageName, Priority.ALWAYS);
				}

				{
					Label l = new Label("Name:");
					pane.add(l, 0, 1);

					className = new TextField();
					pane.add(className, 1, 1);
					GridPane.setHgrow(className, Priority.ALWAYS);
				}

				return pane;
			}

			@Override
			protected void okPressed() {
				resource = handleElementCreation(container,
						packageName.getText(), className.getText());
				if (resource != null) {
					super.okPressed();
				}
			}
		};

		dialog.open();

		return resource;
	}

	IResource handleElementCreation(IContainer container, String packageName,
			String className) {
		// FIXME This is temporary
		if (!container.getProject().isOpen()) {
			try {
				container.getProject().open(new NullProgressMonitor());
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		IProject pr = container.getProject();

		IJavaElement jElement = getInitialJavaElement(container);
		IPackageFragmentRoot jRoot = getFragmentRoot(jElement);

		try {
			IJavaProject jProject = JavaCore.create(pr);
			jProject.open(new NullProgressMonitor());
			jRoot.open(new NullProgressMonitor());
			IPackageFragment fragment = jRoot.getPackageFragment(packageName);
			if( ! fragment.exists() ) {
				((IFolder)fragment.getResource()).create(true, true, null);
			}
			ICompilationUnit u = fragment.getCompilationUnit(className + ".java");
			IFile f = (IFile) u.getResource();
			ByteArrayInputStream in = new ByteArrayInputStream(new String(
					"public class " + className + " {\n}").getBytes());
			f.create(in, IFile.FORCE | IFile.KEEP_HISTORY,
					new NullProgressMonitor());
			in.close();
			// pr.build(IncrementalProjectBuilder.FULL_BUILD, new
			// NullProgressMonitor());
			return f;
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.err.println(jRoot);

		return null;
	}

	private IJavaElement getInitialJavaElement(IContainer container) {
		IJavaElement jelem = null;
		Object selectedElement = container;
		if (selectedElement instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) selectedElement;

			jelem = (IJavaElement) adaptable.getAdapter(IJavaElement.class);
			if (jelem == null || !jelem.exists()) {
				jelem = null;
				IResource resource = (IResource) adaptable
						.getAdapter(IResource.class);
				if (resource != null && resource.getType() != IResource.ROOT) {
					while (jelem == null
							&& resource.getType() != IResource.PROJECT) {
						resource = resource.getParent();
						jelem = (IJavaElement) resource
								.getAdapter(IJavaElement.class);
					}
					if (jelem == null) {
						jelem = JavaCore.create(resource); // java project
					}
				}
			}
		}
		return jelem;
	}

	protected IPackageFragmentRoot getFragmentRoot(IJavaElement elem) {
		IPackageFragmentRoot initRoot = null;
		if (elem != null) {
			initRoot = (IPackageFragmentRoot) elem
					.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
			try {
				if (initRoot == null
						|| initRoot.getKind() != IPackageFragmentRoot.K_SOURCE) {
					IJavaProject jproject = elem.getJavaProject();
					if (jproject != null) {
						initRoot = null;
						if (jproject.exists()) {
							IPackageFragmentRoot[] roots = jproject
									.getPackageFragmentRoots();
							for (int i = 0; i < roots.length; i++) {
								if (roots[i].getKind() == IPackageFragmentRoot.K_SOURCE) {
									initRoot = roots[i];
									break;
								}
							}
						}
						if (initRoot == null) {
							initRoot = jproject.getPackageFragmentRoot(jproject
									.getResource());
						}
					}
				}
			} catch (JavaModelException e) {
				// TODO
				e.printStackTrace();
			}
		}
		return initRoot;
	}

	@Override
	public boolean handles(IResource resource) {
		if (resource instanceof IFile) {
			IFile f = (IFile) resource;
			if ("java".equals(f.getFileExtension())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isRunnable(IResource resource) {
		if (resource instanceof IFile) {
			IFile f = (IFile) resource;
			if ("java".equals(f.getFileExtension())) {
				return true;
			}
		}
		return true;
	}

	@Override
	public void launch(IResource resource) {
		IFile f = (IFile) resource;
		ILaunchManager mgr = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = mgr
				.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
		try {
			ILaunchConfiguration[] configurations = mgr
					.getLaunchConfigurations(type);
			for (int i = 0; i < configurations.length; i++) {
				ILaunchConfiguration configuration = configurations[i];
				if (configuration.getName().equals("Start Class")) {
					configuration.delete();
					break;
				}
			}

			ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(
					null, "Start Class");
			IVMInstall jre = JavaRuntime.getDefaultVMInstall();
			workingCopy.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_NAME,
					jre.getName());
			workingCopy.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_VM_INSTALL_TYPE, jre
							.getVMInstallType().getId());
			
			ICompilationUnit unit = (ICompilationUnit) JavaCore.create(f);
			workingCopy.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, unit.findPrimaryType().getFullyQualifiedName());

			IJavaProject jp = JavaCore.create(resource.getProject());

			IRuntimeClasspathEntry pr = JavaRuntime
					.newDefaultProjectClasspathEntry(jp);
			IPath systemLibsPath = new Path(JavaRuntime.JRE_CONTAINER);
			IRuntimeClasspathEntry systemLibsEntry = JavaRuntime
					.newRuntimeContainerClasspathEntry(systemLibsPath,
							IRuntimeClasspathEntry.STANDARD_CLASSES);
			List<String> classpath = new ArrayList<String>();
			classpath.add(systemLibsEntry.getMemento());
			classpath.add(pr.getMemento());
			workingCopy
					.setAttribute(
							IJavaLaunchConfigurationConstants.ATTR_CLASSPATH,
							classpath);
			workingCopy.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH,
					false);
			ILaunchConfiguration configuration = workingCopy.doSave();
			ILaunch l = configuration.launch(ILaunchManager.RUN_MODE,
					new NullProgressMonitor());
			IProcess p = l.getProcesses()[0];
			
			final StringBuffer b = new StringBuffer();
			Stage s = new Stage(StageStyle.UTILITY);
			s.setTitle("Console");
			final WebView view = new WebView();
			s.setScene(new Scene(view));
			s.setWidth(600);
			s.setHeight(400);
			s.show();
			
			IStreamMonitor out = p.getStreamsProxy().getOutputStreamMonitor();
			out.addListener(new IStreamListener() {

				@Override
				public void streamAppended(String text, IStreamMonitor monitor) {
					b.append(text.replace("\n", "<br />"));
					Platform.runLater(new Runnable() {
						
						@Override
						public void run() {
							view.getEngine().loadContent("<html><body style='font-family: Verdana, Helvetica;'>" + b.toString() + "</body></html>");
						}
					});
				}
			});

			IStreamMonitor err = p.getStreamsProxy().getErrorStreamMonitor();
			err.addListener(new IStreamListener() {

				@Override
				public void streamAppended(String text, IStreamMonitor monitor) {
					b.append("<font color='red'>"+text.replace("\n", "<br />")+"</font>");
					Platform.runLater(new Runnable() {
						
						@Override
						public void run() {
							view.getEngine().loadContent("<html><body style='font-family: Verdana, Helvetica;'>" + b.toString() + "</body></html>");
						}
					});
				}
			});

		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
