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
package at.bestsolution.javafx.ide.services;

import javafx.stage.Stage;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;

public interface IResourceService {
	public String getIconUri();
	public String getLabel();
	public IResource create(IContainer container, Stage parent);
	public boolean handles(IResource resource);
	public boolean isRunnable(IResource resource);
	public void launch(IResource resource);
}
