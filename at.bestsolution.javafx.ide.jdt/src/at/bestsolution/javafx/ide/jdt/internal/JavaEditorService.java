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
package at.bestsolution.javafx.ide.jdt.internal;

import at.bestsolution.javafx.ide.services.IEditorInput;
import at.bestsolution.javafx.ide.services.IEditorService;
import at.bestsolution.javafx.ide.services.IResourceFileInput;

public class JavaEditorService implements IEditorService {

	@Override
	public String getIconUri() {
		return "platform:/plugin/at.bestsolution.javafx.ide.jdt/icons/16_16/jcu_resource_obj.gif";
	}

	@Override
	public Class<?> getEditorClass() {
		return JavaEditor.class;
	}

	@Override
	public boolean handlesInput(IEditorInput input) {
		if (input instanceof IResourceFileInput) {
			IResourceFileInput fs = (IResourceFileInput) input;
			return "java".equals(fs.getFile().getFileExtension());
		}
		return false;
	}

	@Override
	public String getTitle(IEditorInput input) {
		if (input instanceof IResourceFileInput) {
			IResourceFileInput fs = (IResourceFileInput) input;
			return fs.getFile().getName();
		}
		return "Java Editor";
	}
}