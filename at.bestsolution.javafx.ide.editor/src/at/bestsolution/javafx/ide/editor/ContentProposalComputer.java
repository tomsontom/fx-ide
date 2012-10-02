package at.bestsolution.javafx.ide.editor;

import java.util.List;

public interface ContentProposalComputer {
	enum Type {
		METHOD,
		FIELD,
		TYPE
	}
	
	public static class Proposal {
		public final Type type;
		public final String value;
		public final String description;
		
		public Proposal(Type type, String value, String description) {
			this.type = type;
			this.value = value;
			this.description = description;
		}
	}
	
	public List<Proposal> computeProposals(String line, String prefix, int offset);
}
