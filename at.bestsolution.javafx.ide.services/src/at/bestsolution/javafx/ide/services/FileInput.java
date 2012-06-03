package at.bestsolution.javafx.ide.services;

import org.eclipse.core.resources.IFile;

public class FileInput implements IResourceFileInput {
		private IFile f;
		
		public FileInput(IFile f) {
			this.f = f;
		}
		
		@Override
		public IFile getFile() {
			return f;
		}
	}