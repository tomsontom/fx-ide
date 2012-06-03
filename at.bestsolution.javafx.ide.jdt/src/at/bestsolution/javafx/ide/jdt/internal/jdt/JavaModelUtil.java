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

import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CharOperation;

public class JavaModelUtil {
	public static boolean isExcluded(IPath resourcePath, char[][] exclusionPatterns) {
		if (exclusionPatterns == null) return false;
		char[] path = resourcePath.toString().toCharArray();
		for (int i = 0, length = exclusionPatterns.length; i < length; i++)
			if (CharOperation.pathMatch(exclusionPatterns[i], path, true, '/'))
				return true;
		return false;
	}
	
	public static boolean isExcludedPath(IPath resourcePath, IPath[] exclusionPatterns) {
		char[] path = resourcePath.toString().toCharArray();
		for (int i = 0, length = exclusionPatterns.length; i < length; i++) {
			char[] pattern= exclusionPatterns[i].toString().toCharArray();
			if (CharOperation.pathMatch(pattern, path, true, '/')) {
				return true;
			}
		}
		return false;
	}
	
	public static void setComplianceOptions(Map<String, String> map, String compliance) {
		JavaCore.setComplianceOptions(compliance, map);
	}
	
	public static void setDefaultClassfileOptions(Map<String, String> map, String compliance) {
		map.put(JavaCore.COMPILER_CODEGEN_INLINE_JSR_BYTECODE, is50OrHigher(compliance) ? JavaCore.ENABLED : JavaCore.DISABLED);
		map.put(JavaCore.COMPILER_LOCAL_VARIABLE_ATTR, JavaCore.GENERATE);
		map.put(JavaCore.COMPILER_LINE_NUMBER_ATTR, JavaCore.GENERATE);
		map.put(JavaCore.COMPILER_SOURCE_FILE_ATTR, JavaCore.GENERATE);
		map.put(JavaCore.COMPILER_CODEGEN_UNUSED_LOCAL, JavaCore.PRESERVE);
	}
	
	public static boolean is50OrHigher(String compliance) {
		return !isVersionLessThan(compliance, JavaCore.VERSION_1_5);
	}
	
	public static boolean isVersionLessThan(String version1, String version2) {
		if (JavaCore.VERSION_CLDC_1_1.equals(version1)) {
			version1= JavaCore.VERSION_1_1 + 'a';
		}
		if (JavaCore.VERSION_CLDC_1_1.equals(version2)) {
			version2= JavaCore.VERSION_1_1 + 'a';
		}
		return version1.compareTo(version2) < 0;
	}
}
