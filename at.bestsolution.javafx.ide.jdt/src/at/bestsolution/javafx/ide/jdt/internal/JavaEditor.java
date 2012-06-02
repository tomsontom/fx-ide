package at.bestsolution.javafx.ide.jdt.internal;

import java.io.IOException;
import java.io.InputStream;

import javafx.scene.layout.BorderPane;

import javax.inject.Inject;

import org.eclipse.core.runtime.CoreException;

import at.bestsolution.javafx.ide.editor.Document;
import at.bestsolution.javafx.ide.editor.SourceEditor;
import at.bestsolution.javafx.ide.services.IEditorInput;
import at.bestsolution.javafx.ide.services.IResourceFileInput;

public class JavaEditor {
	@Inject
	public JavaEditor(BorderPane pane, IEditorInput input) {
		SourceEditor editor = new SourceEditor();
		pane.setCenter(editor);
		
		editor.setDocument(createDocument(input));
	}
	
	private Document createDocument(IEditorInput input) {
		IResourceFileInput fsInput = (IResourceFileInput) input;
		try(InputStream in = fsInput.getFile().getContents();) {
			byte[] buf = new byte[1024];
			int l = 0;
			StringBuilder b = new StringBuilder();
			while( (l = in.read(buf)) != -1 ) {
				b.append(new String(buf,0,l));
			}
			
			return new StringDocument(b);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return new StringDocument("/** Unable to extract content from '"+input+"' **/");
	}
	
	static class StringDocument implements Document {
		private StringBuilder b;
		
		public StringDocument(CharSequence initialContent) {
			b = new StringBuilder(initialContent);
		}
		
		@Override
		public String get() {
			return b.toString();
		}
		
	}
}
