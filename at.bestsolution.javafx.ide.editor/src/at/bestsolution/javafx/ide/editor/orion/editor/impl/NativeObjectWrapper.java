package at.bestsolution.javafx.ide.editor.orion.editor.impl;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.web.WebEngine;
import javafx.util.Callback;

import netscape.javascript.JSObject;

public class NativeObjectWrapper {
	private List<Pair> eventListeners = new ArrayList<>();
	
	static class Pair {
		private final String type;
		private final Callback eventListener;
		
		public Pair(String type, Callback<? extends Object, Void> eventListener) {
			this.type = type;
			this.eventListener = eventListener;
		}
	}
	
	protected WebEngine e;
	
	public NativeObjectWrapper(WebEngine e, JSObject jsObject) {
		this.e = e;
		if( jsObject != null ) {
			jsObject.setMember("__javaObject", this);	
		}
	}
	
	protected JSObject getJSObject() {
		JSObject win = (JSObject) e.executeScript("window");
		return (JSObject) win.getMember(getClass().getSimpleName());
	}
		
	public void addEventListener(String type, Callback<? extends Object, Void> callback) {
		eventListeners.add(new Pair(type,callback));
	}
	
	public void _js_Event(String type, String eventObjectType, Object data) {
		Object jObject;
//		System.err.println(type + " => " + eventObjectType + " =>" + data );
		if( data instanceof JSObject ) {
			jObject = createWrapper((JSObject) data, eventObjectType);
		} else {
			jObject = data;
		}
		
		for( Pair e : eventListeners ) {
			if( e.type.equals(type) ) {
				e.eventListener.call(jObject);
			}
		}
	}
	
	protected Object createWrapper(JSObject object, String type) {
		return object;
	}
}
