package at.bestsolution.javafx.ide.jdt.internal.jdt;

import org.eclipse.core.runtime.IPath;

public class ArchiveFileFilter {
	private static final String[] fgArchiveExtensions= { "jar", "zip" }; //$NON-NLS-1$ //$NON-NLS-2$

	public static boolean isArchivePath(IPath path, boolean allowAllAchives) {
		if (allowAllAchives)
			return true;

		String ext= path.getFileExtension();
		if (ext != null && ext.length() != 0) {
			return isArchiveFileExtension(ext);
		}
		return false;
	}
	
	public static boolean isArchiveFileExtension(String ext) {
		for (int i= 0; i < fgArchiveExtensions.length; i++) {
			if (ext.equalsIgnoreCase(fgArchiveExtensions[i])) {
				return true;
			}
		}
		return false;
	}
}
