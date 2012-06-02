package at.bestsolution.javafx.ide.jdt.internal.jdt;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;

public class BuildPathSupport {

	public static final String JRE_PREF_PAGE_ID= "org.eclipse.jdt.debug.ui.preferences.VMPreferencePage"; //$NON-NLS-1$
	public static final String EE_PREF_PAGE_ID= "org.eclipse.jdt.debug.ui.jreProfiles"; //$NON-NLS-1$
	
	/* see also ComplianceConfigurationBlock#PREFS_COMPLIANCE */
	private static final String[] PREFS_COMPLIANCE= new String[] {
			JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.COMPILER_PB_ENUM_IDENTIFIER,
			JavaCore.COMPILER_SOURCE, JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM,
			JavaCore.COMPILER_COMPLIANCE
	};
	
	public static String getDeprecationMessage(String varName) {
		return "TODO DEPRECATED";
	}

	public static void setEEComplianceOptions(IJavaProject javaProject, String eeID, String newProjectCompliance) {
		IExecutionEnvironment ee= JavaRuntime.getExecutionEnvironmentsManager().getEnvironment(eeID);
		if (ee != null) {
			Map<String, String> options= javaProject.getOptions(false);
			Map<String, String> eeOptions= getEEOptions(ee);
			if (eeOptions != null) {
				for (int i= 0; i < PREFS_COMPLIANCE.length; i++) {
					String option= PREFS_COMPLIANCE[i];
					options.put(option, eeOptions.get(option));
				}
				
				if (newProjectCompliance != null) {
					JavaModelUtil.setDefaultClassfileOptions(options, newProjectCompliance); // complete compliance options
				}
				
				String option= JavaCore.COMPILER_CODEGEN_INLINE_JSR_BYTECODE;
				String inlineJSR= eeOptions.get(option);
				if (inlineJSR != null) {
					options.put(option, inlineJSR);
				}
				
				javaProject.setOptions(options);
			}
		}
	}
	
	public static Map<String, String> getEEOptions(IExecutionEnvironment ee) {
		if (ee == null)
			return null;
		Map<String, String> eeOptions= ee.getComplianceOptions();
		if (eeOptions == null)
			return null;
		
		Object complianceOption= eeOptions.get(JavaCore.COMPILER_COMPLIANCE);
		if (!(complianceOption instanceof String))
			return null;
	
		// eeOptions can miss some options, make sure they are complete:
		HashMap<String, String> options= new HashMap<String, String>();
		JavaModelUtil.setComplianceOptions(options, (String)complianceOption);
		options.putAll(eeOptions);
		return options;
	}

}
