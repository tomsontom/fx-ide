package at.bestsolution.javafx.ide.editor;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.EventHandler;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import at.bestsolution.javafx.ide.editor.js.ProxyNCICallback;
import at.bestsolution.javafx.ide.editor.orion.editor.Editor;

public class SourceEditor extends BorderPane {
	private WebView webView;
	private Editor editor;
	private Document document;
	
	public SourceEditor() {
		this.webView = new WebView();
		this.webView.setFontSmoothingType(FontSmoothingType.GRAY);
		final WebEngine e = this.webView.getEngine();
		
		e.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {

			@Override
			public void changed(ObservableValue<? extends State> observable,
					State oldValue, State newValue) {
				if (newValue == State.SUCCEEDED) {
					JSObject win = (JSObject) e.executeScript("window");
					win.setMember("javaEditor", new ProxyNCICallback<Editor>(Editor.class) {

						@Override
						protected void init(Editor o) {
							initEditor(o);
						}
					});
				}
			}
		});
		
		e.load("file:///Users/tomschindl/git/fx-ide/at.bestsolution.javafx.ide.editor/orion/html/editor.html");
		e.setOnAlert(new EventHandler<WebEvent<String>>() {
			
			@Override
			public void handle(WebEvent<String> event) {
				System.err.println("This is an alert: " + event);
			}
		});
		setCenter(webView);
	}
	
	void initEditor(Editor editor) {
		this.editor = editor;
		if( document != null ) {
			editor.setInput("myclass.java", null, document.get());
		}
	}
	
	public void setDocument(Document document) {
		this.document = document;
		if( editor != null ) {
			editor.setInput("myclass.java", null, document.get());
		}
	}
}
