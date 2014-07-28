package edu.ucsc.codevo.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.taskdefs.Javac;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;

public class ProjectReconfigurer {
	public static void reconfigure(IJavaProject javaProject, IProgressMonitor monitor) throws CoreException, IOException {
		IProject eclipseProject = javaProject.getProject();
		File buildFile = eclipseProject.getFile("build.xml").getLocation().toFile();
		Project antProject = new Project();
		antProject.init();
		antProject.setUserProperty(MagicNames.ANT_FILE,
                buildFile.getAbsolutePath());
		antProject.setUserProperty(MagicNames.ANT_FILE_TYPE,
                MagicNames.ANT_FILE_TYPE_FILE);
		ProjectHelper.configureProject(antProject, buildFile);
		Task[] tasks = antProject.getTargets().get("build").getTasks();
		for (Task task : tasks) {                                                                                                                                                                            
		    if (task instanceof UnknownElement) {                                                                                                                                                            
		        ((UnknownElement)task).maybeConfigure();                                                                                                                                                     
		        task = ((UnknownElement)task).getTask();                                                                                                                                                     
		        if (task == null) {                                                                                                                                                                          
		            continue;                                                                                                                                                                               
		        }                                                                                                                                                                                            
		    }                                                                                                                                                                                                
		    if (task instanceof Javac) {
		    	Javac javacTask = (Javac)task;
		        configureClasspath(javaProject, javacTask, monitor);
				break;
		    }                                                                                                                                                                                                
		}
		eclipseProject.refreshLocal(IProject.DEPTH_INFINITE, monitor);
	}

	private static void configureClasspath(IJavaProject javaProject, Javac javacTask,
			IProgressMonitor monitor) throws CoreException, IOException {
		ArrayList<IClasspathEntry> entries = new ArrayList<>();
		IProject eclipseProject = javaProject.getProject();
		// add source directories
		File destdir = javacTask.getDestdir();
		if (destdir.exists()) {
			FileUtils.cleanDirectory(destdir);
		}
		org.apache.tools.ant.types.Path sourceDirs = javacTask.getSrcdir();
		for (String srcDir : sourceDirs.list()) {
			IFolder folder = eclipseProject.getFolder(new File(srcDir).getName());
			folder.createLink(
					new Path(srcDir), 
					IResource.ALLOW_MISSING_LOCAL | IResource.REPLACE, 
					monitor);
			entries.add(JavaCore.newSourceEntry(folder.getFullPath()));
		}
		// add libraries
		org.apache.tools.ant.types.Path classpath = 
				javacTask.getClasspath();                                                                                                                                                               
		if (classpath != null) {
			for (String cp : classpath.list()) {
				File classpathEntry = new File(cp);
				IPath path = new Path(classpathEntry.getAbsolutePath());
				if (classpathEntry.isDirectory()) {
					IFolder folder = eclipseProject.getFolder(path.lastSegment());
					folder.createLink(
							path, 
							IResource.ALLOW_MISSING_LOCAL | IResource.REPLACE, 
							monitor);
				}
				entries.add(JavaCore.newLibraryEntry(
						path, null, null));
			}
		}
		// add JRE
		entries.add(
				JavaCore.newVariableEntry(
						new Path(JavaRuntime.JRELIB_VARIABLE), 
						new Path(JavaRuntime.JRESRC_VARIABLE), 
						new Path(JavaRuntime.JRESRCROOT_VARIABLE)));
		javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), monitor);
	}
}
