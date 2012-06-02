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
