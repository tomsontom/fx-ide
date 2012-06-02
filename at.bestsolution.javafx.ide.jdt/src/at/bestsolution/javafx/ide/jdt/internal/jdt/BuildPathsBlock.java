package at.bestsolution.javafx.ide.jdt.internal.jdt;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class BuildPathsBlock {
	public static boolean hasClassfiles(IResource resource) throws CoreException {
		if (resource.isDerived()) {
			return true;
		}
		if (resource instanceof IContainer) {
			IResource[] members= ((IContainer) resource).members();
			for (int i= 0; i < members.length; i++) {
				if (hasClassfiles(members[i])) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static void removeOldClassfiles(IResource resource) throws CoreException {
		if (resource.isDerived()) {
			resource.delete(false, null);
		} else if (resource instanceof IContainer) {
			IResource[] members= ((IContainer) resource).members();
			for (int i= 0; i < members.length; i++) {
				removeOldClassfiles(members[i]);
			}
		}
	}
}
