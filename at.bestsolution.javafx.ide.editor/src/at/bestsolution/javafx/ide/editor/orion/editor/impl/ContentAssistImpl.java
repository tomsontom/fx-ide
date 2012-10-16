package at.bestsolution.javafx.ide.editor.orion.editor.impl;

import java.util.List;

import netscape.javascript.JSObject;
import at.bestsolution.javafx.ide.editor.ContentProposalComputer;
import at.bestsolution.javafx.ide.editor.ContentProposalComputer.Proposal;
import at.bestsolution.javafx.ide.editor.ContentProposalComputer.Segment;
import at.bestsolution.javafx.ide.editor.ContentProposalComputer.StyledString;
import at.bestsolution.javafx.ide.editor.ContentProposalComputer.Type;
import at.bestsolution.javafx.ide.editor.orion.editor.ContentAssist;

@SuppressWarnings("restriction")
public class ContentAssistImpl implements ContentAssist {
	private ContentProposalComputer computer;
	
	public ContentAssistImpl(ContentProposalComputer computer, JSObject jsObject) {
		jsObject.setMember("__javaObject", this);
		this.computer = computer;
	}
	
	public String[] testMe() {
		return new String[] {"h1","h2"};
	}

	@Override
	public String getProposals(String line, String prefix, int offset) {
		StringBuilder b = new StringBuilder();
		b.append("[");
		boolean flag = false;
		for( Proposal p : computer.computeProposals(line, prefix, offset) ) {
			if(flag) {
				b.append(",\n");
			}
			String v = p.value;
//			if( prefix.length() > 0 ) {
//				v = v.substring(prefix.length());
//			}
			
			b.append("	{ ");
			b.append("\"proposal\": \""+v+"\" ");
			
			String description = "";
			
//			if( p.type == Type.TYPE ) {
//				description = "<div class='content-item class-item'><nobr>" + toHTML(p.description) + "</nobr></div>";
//			} else if(p.type == Type.METHOD) {
//				description = "<div class='content-item method-item'><nobr>" + toHTML(p.description) + "</nobr></div>";
//			} else if(p.type == Type.METHOD) {
//				description = "<div class='content-item field-item'><nobr>" + toHTML(p.description) + "</nobr></div>";
//			} else {
//				description = toHTML(p.description);
//			}
			
			if( p.type == Type.TYPE ) {
				description = "{ \"styleClass\": \"content-item class-item\",  \"segments\": " + toJSON(p.description) + "}";
			} else if(p.type == Type.METHOD) {
				description = "{ \"styleClass\": \"content-item method-item\",  \"segments\": " + toJSON(p.description) + "}";
			} else if(p.type == Type.METHOD) {
				description = "{ \"styleClass\": \"content-item field-item\",  \"segments\": " + toJSON(p.description) + "}";
			} else {
				description = "{ \"segments\": " + toJSON(p.description) + "}";
			}
			
			b.append(",\"description\":  "+description+" ");
			if( p.type == Type.METHOD ) {
				b.append(",\"escapePosition\": "+(offset+v.length()-1)+" ");	
			}
			b.append(", \"style\": \"attributedString\"");
			b.append(", \"replace\":  true");
			
			b.append("}");
			flag = true;
		}
		b.append("]");
		
		System.err.println(b.toString()); 
		return b.toString();
	}

	private static String toJSON(StyledString s) {
		StringBuilder b = new StringBuilder();
		b.append("[");
		boolean flag = false;
		for( Segment segment : s.getList() ) {
			if( flag ) {
				b.append(",");
			}
			b.append("{");
			b.append("\"value\":\"" +segment.value+"\"");
			if( segment.style != null ) {
				b.append(", \"style\": {");
				b.append("\"bold\":  " + segment.style.bold + " ");
				b.append(", \"italic\": " + segment.style.italic + " ");
				if(segment.style.color != null) {
					b.append(", \"color\":\"" + segment.style.color + "\"");
				}
				if(segment.style.fontname != null) {
					b.append(", \"fontname\":\"" + segment.style.fontname + "\"");
				}
				b.append("");
				b.append("}");
			}
			b.append("}");
			flag = true;
		}
		b.append("]");
		return b.toString();
	}
	
	private static String toHTML(StyledString s) {
		StringBuilder b = new StringBuilder();
		b.append("<span>");
		for( Segment segment : s.getList() ) {
			if( segment.style == null ) {
				b.append(segment.value);
			} else {
				String styleString = "";
				if( segment.style.bold ) {
					styleString += "font-weight: bold;";
				}
				
				if(segment.style.italic) {
					styleString += "font-style: italic;";
				}
				
				if(segment.style.color != null) {
					styleString += "color: " + segment.style.color + ";";
				}
				
				if(segment.style.fontname != null) {
					styleString += "font-name: " + segment.style.fontname + ";";
				}
				b.append("<span style='"+styleString+"'>"+segment.value+"</span>");
			}
		}
		b.append("</span>");
		return b.toString();
	}
}
