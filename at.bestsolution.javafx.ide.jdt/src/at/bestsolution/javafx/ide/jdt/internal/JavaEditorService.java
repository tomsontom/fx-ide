package at.bestsolution.javafx.ide.jdt.internal;

import at.bestsolution.javafx.ide.services.IEditorInput;
import at.bestsolution.javafx.ide.services.IEditorService;
import at.bestsolution.javafx.ide.services.IResourceFileInput;

public class JavaEditorService implements IEditorService {

	@Override
	public String getIconUri() {
		return "platform:/plugin/at.bestsolution.javafx.ide.jdt/icons/16_16/jcu_resource_obj.gif";
	}

	@Override
	public Class<?> getEditorClass() {
		return JavaEditor.class;
	}

	@Override
	public boolean handlesInput(IEditorInput input) {
		if (input instanceof IResourceFileInput) {
			IResourceFileInput fs = (IResourceFileInput) input;
			return "java".equals(fs.getFile().getFileExtension());
		}
		return false;
	}

	@Override
	public String getTitle(IEditorInput input) {
		if (input instanceof IResourceFileInput) {
			IResourceFileInput fs = (IResourceFileInput) input;
			return fs.getFile().getName();
		}
		return "Java Editor";
	}
}