package at.bestsolution.javafx.ide.editor.orion.editor.impl;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.web.WebEngine;

import netscape.javascript.JSObject;
import at.bestsolution.javafx.ide.editor.orion.editor.Editor;
import at.bestsolution.javafx.ide.editor.orion.textview.TextView;
import at.bestsolution.javafx.ide.editor.orion.textview.impl.TextViewImpl;

public class EditorImpl extends NativeObjectWrapper implements Editor {
	private Map<String, Runnable> actionSet = new HashMap<>();
	
	public EditorImpl(WebEngine e, JSObject jsObject) {
		super(e, jsObject);
	}

	public TextView getTextView() {
		JSObject jsTextView = (JSObject) getJSObject().call("getTextView");
		Object o = jsTextView.getMember("__javaObject");
		if( o != null && o instanceof TextView ) {
			return (TextView) o;
		} else {
			return new TextViewImpl(e, jsTextView);
		}
	}
	
	@Override
	public void setInput(String title, String message, String contents) {
		getJSObject().call("setInput", title, message, contents);
	}

	public void setAction(String action, Runnable actionRunnable) {
		actionSet.put(action, actionRunnable);
	}
	
	public void _js_Action(String action) {
		if( actionSet.containsKey(action) ) {
			actionSet.get(action).run();
		}
	}
	
	public void _js_StatusReporter(Object message, Object error) {
		System.err.println("Status-Report: " + message + " => " + error);
	}

	@Override
	public boolean isDirty() {
		Object o = getJSObject().call("isDirty");
		if( o instanceof Boolean ) {
			return ((Boolean)o).booleanValue();
		}
		return false;
	}

	
}
