#!/bin/sh

cd target/macosx.cocoa.x86_64/eclipse 
/Library/Java/JavaVirtualMachines/jdk1.7.0_08.jdk/Contents/Home/bin/java -cp plugins/org.eclipse.osgi_3.8.1.v20120830-144521.jar:plugins/at.bestsolution.efxclipse.runtime.osgi_0.1.1.201210182224.jar org.eclipse.core.runtime.adaptor.EclipseStarter -configuration configuration -consoleLog -nosplash -console -application at.bestsolution.javafx.ide.editor.app.application
