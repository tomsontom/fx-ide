package at.bestsolution.javafx.ide.editor.orion.editor.impl;

import java.util.List;

import netscape.javascript.JSObject;
import at.bestsolution.javafx.ide.editor.ContentProposalComputer;
import at.bestsolution.javafx.ide.editor.ContentProposalComputer.Proposal;
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
			if( prefix.length() > 0 ) {
				v = v.substring(prefix.length());
			}
			
			b.append("	{ ");
			b.append("\"proposal\": \""+v+"\" ");
			b.append(",\"description\": \""+p.description+"\" ");
			if( p.type == Type.METHOD ) {
				b.append(",\"escapePosition\": "+(offset+v.length()-1)+" ");	
			}
			
			b.append("}");
			flag = true;
		}
		b.append("]");
		return b.toString();
	}

}
