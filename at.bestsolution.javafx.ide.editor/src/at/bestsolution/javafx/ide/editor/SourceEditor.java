package at.bestsolution.javafx.ide.editor;

import javafx.scene.Group;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class SourceEditor extends Group {
	private WebView webView;
	public SourceEditor() {
		this.webView = new WebView();
		WebEngine e = this.webView.getEngine();
		e.loadContent("file:///Users/tomschindl/git/fx-ide/at.bestsolution.javafx.ide.editor/orion/html/editor.html");
		getChildren().add(webView);
	}
}
