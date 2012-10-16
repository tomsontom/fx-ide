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

public class ProblemMarker {
	public enum Type {
		WARNING,
		ERROR
	}
	
	public final Type type;
	public final int linenumber;
	public final int startCol;
	public final int endCol;
	public final String description;
	
	public ProblemMarker(Type type, int linenumber, int startCol, int endCol, String description) {
		this.type = type;
		this.linenumber = linenumber;
		this.startCol = startCol;
		this.endCol = endCol;
		this.description = description;
	}

	@Override
	public String toString() {
		return "ProblemMarker [type=" + type + ", linenumber=" + linenumber + ", startCol=" + startCol + ", endCol=" + endCol + ", description=" + description + "]";
	}
	
	
}
