/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package at.bestsolution.javafx.ide.jdt.internal.jdt;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class JavaPlugin implements BundleActivator {
	public static final String ID_PLUGIN = "at.bestsolution.javafx.ide.jdt";
	private ClasspathAttributeConfigurationDescriptors fClasspathAttributeConfigurationDescriptors;
	
	private static JavaPlugin INSTANCE;
	
	public JavaPlugin() {
		INSTANCE = this;
	}
	
	public static void logErrorMessage(String string) {
		System.err.println(string);
	}

	public static final JavaPlugin getDefault() {
		return INSTANCE;
	}
	
	public ClasspathAttributeConfigurationDescriptors getClasspathAttributeConfigurationDescriptors() {
		if (fClasspathAttributeConfigurationDescriptors == null) {
			fClasspathAttributeConfigurationDescriptors= new ClasspathAttributeConfigurationDescriptors();
		}
		return fClasspathAttributeConfigurationDescriptors;
	}

	public static void log(Throwable exception) {
		exception.printStackTrace();
	}


	@Override
	public void start(BundleContext context) throws Exception {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
