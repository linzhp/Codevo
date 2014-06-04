package codevo.handlers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SampleHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public SampleHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		try {
			IWorkspaceRoot root = workspace.getRoot();
			IProject project = root.getProject("fixtureProject");
			project.create(null);
			project.open(null);
			
			IProjectDescription description = project.getDescription();
			description.setNatureIds(new String[] { JavaCore.NATURE_ID });
			project.setDescription(description, null);
			
			IJavaProject javaProject = JavaCore.create(project);
			
			List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
//			IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
//			LibraryLocation[] locations = JavaRuntime.getLibraryLocations(vmInstall);
//			for (LibraryLocation element : locations) {
//			 entries.add(JavaCore.newLibraryEntry(element.getSystemLibraryPath(), null, null));
//			}
			FileUtils.copyDirectoryToDirectory(new File("E:\\Development\\Codevo\\fixtures\\src"), project.getLocation().toFile());
			project.refreshLocal(IProject.DEPTH_INFINITE, null);
			entries.add(JavaCore.newSourceEntry(project.getFullPath().append("src")));
			javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
			IPackageFragment[] packages = javaProject.getPackageFragments();
			for (IPackageFragment p : packages) {
				if (p.getKind() == IPackageFragmentRoot.K_SOURCE) {
					for (ICompilationUnit unit : p.getCompilationUnits()) {
						ASTParser parser = ASTParser.newParser(AST.JLS4);
						parser.setKind(ASTParser.K_COMPILATION_UNIT);
						parser.setSource(unit);
						parser.setResolveBindings(true);
						ASTNode ast = parser.createAST(null);
//						ast.accept(analyzer);
					}
				}
			}
		} catch (IOException | CoreException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		return null;
	}
}
