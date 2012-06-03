package at.bestsolution.javafx.ide.jdt.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javafx.scene.layout.BorderPane;

import javax.inject.Inject;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import at.bestsolution.javafx.ide.editor.Document;
import at.bestsolution.javafx.ide.editor.SourceEditor;
import at.bestsolution.javafx.ide.services.IEditorInput;
import at.bestsolution.javafx.ide.services.IResourceFileInput;

public class JavaEditor {
	private IResourceFileInput fsInput;
	
	@Inject
	public JavaEditor(BorderPane pane, IEditorInput input) {
		SourceEditor editor = new SourceEditor();
		pane.setCenter(editor);
		
		final Document doc = createDocument(input);
		editor.setDocument(doc);
		editor.setSaveCallback(new Runnable() {
			
			@Override
			public void run() {
				if( JavaEditor.this.fsInput != null ) {
					InputStream in = new ByteArrayInputStream(doc.get().getBytes());
					try {
						fsInput.getFile().setContents(in, IResource.FORCE|IResource.KEEP_HISTORY, new NullProgressMonitor());
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	private Document createDocument(IEditorInput input) {
		fsInput = (IResourceFileInput) input;
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
		private StringBuilder content;
		
		public StringDocument(CharSequence initialContent) {
			content = new StringBuilder(initialContent);
		}
		
		@Override
		public String get() {
			return content.toString();
		}

		@Override
		public void insert(int start, String data) {
			content.insert(start, data);
		}
		
		@Override
		public void set(String data) {
			content = new StringBuilder(data);
		}
	}
}
