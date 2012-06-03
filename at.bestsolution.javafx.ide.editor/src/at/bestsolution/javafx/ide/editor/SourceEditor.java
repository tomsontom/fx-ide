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
import javafx.util.Callback;
import netscape.javascript.JSObject;
import at.bestsolution.javafx.ide.editor.js.JavaScriptNCICallback;
import at.bestsolution.javafx.ide.editor.orion.editor.Editor;
import at.bestsolution.javafx.ide.editor.orion.editor.impl.EditorImpl;
import at.bestsolution.javafx.ide.editor.orion.textview.ModelChangedEvent;

public class SourceEditor extends BorderPane {
	private WebView webView;
	private Editor editor;
	private Document document;
	private Runnable runnable;
	
	public SourceEditor() {
		this.webView = new WebView();
		this.webView.setFontSmoothingType(FontSmoothingType.GRAY);
		final WebEngine engine = this.webView.getEngine();
		
		engine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {

			@Override
			public void changed(ObservableValue<? extends State> observable,
					State oldValue, State newValue) {
				if (newValue == State.SUCCEEDED) {
					JSObject win = (JSObject) engine.executeScript("window");
					win.setMember("javaEditor", new JavaScriptNCICallback<JSObject>() {

						@Override
						public void initJava(JSObject jsObject) {
							EditorImpl e = new EditorImpl(engine, jsObject); 
							initEditor(e);
						}
					}); 
				}
			}
		});
		
		engine.load("file:///Users/tomschindl/git/fx-ide/at.bestsolution.javafx.ide.editor/orion/html/editor.html");
		engine.setOnAlert(new EventHandler<WebEvent<String>>() {
			
			@Override
			public void handle(WebEvent<String> event) {
//				System.err.println("This is an alert: " + event);
			}
		});
		setCenter(webView);
	}
	
	void initEditor(final Editor editor) { 
		this.editor = editor;
		
		if( document != null ) {
			editor.setInput("myclass.java", null, document.get());
		}
		
		this.editor.getTextView().addEventListener("ModelChanged", new Callback<ModelChangedEvent, Void>() {
			
			@Override
			public Void call(ModelChangedEvent param) {
				if( param.getRemovedCharCount() == 0 && param.getRemovedLineCount() == 0 ) {
					int start = param.getStart();
					int end = start + param.getAddedCharCount();
					document.insert(param.getStart(), SourceEditor.this.editor.getTextView().getText(start,end));
				} else {
					document.set(SourceEditor.this.editor.getTextView().getText());
				}
				return null;
			}
		});
		
		this.editor.setAction("Save", new Runnable() {
			
			@Override
			public void run() {
				if( runnable != null ) {
					runnable.run();
				}
			}
		});
	}
	
	public void setDocument(Document document) {
		this.document = document;
		if( editor != null ) {
			editor.setInput("myclass.java", null, document.get());
		}
	}
	
	public void setSaveCallback(Runnable runnable) {
		this.runnable = runnable;
	}
}
