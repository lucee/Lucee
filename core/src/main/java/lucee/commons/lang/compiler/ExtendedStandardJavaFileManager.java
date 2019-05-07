package lucee.commons.lang.compiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

/**
 * Created by trung on 5/3/15. Edited by turpid-monkey on 9/25/15, completed support for multiple
 * compile units.
 */
public class ExtendedStandardJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

	private List<CompiledCode> compiledCode = new ArrayList<CompiledCode>();
	private DynamicClassLoader cl;

	/**
	 * Creates a new instance of ForwardingJavaFileManager.
	 *
	 * @param fileManager delegate to this file manager
	 * @param cl
	 */
	protected ExtendedStandardJavaFileManager(JavaFileManager fileManager, DynamicClassLoader cl) {
		super(fileManager);
		this.cl = cl;
	}

	@Override
	public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
		// print.e("listt:" + location);
		// print.e(packageName);
		/*
		 * Iterable<JavaFileObject> it = super.list(location, packageName, kinds, recurse); for
		 * (JavaFileObject jfo: it) { print.e("- " + jfo); }
		 */
		return super.list(location, packageName, kinds, recurse);
	}

	@Override
	public String inferBinaryName(Location location, JavaFileObject file) {
		// print.e("inferBinary:" + location);
		// print.e(file);
		return super.inferBinaryName(location, file);
	}

	@Override
	public boolean hasLocation(Location location) {
		// print.e("has" + location);
		return super.hasLocation(location);
	}

	@Override
	public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
		// print.e("forInput:" + location);
		// print.e(packageName);
		// print.e(relativeName);
		return super.getFileForInput(location, packageName, relativeName);
	}

	/*
	 * @Override public String inferModuleName(Location location) throws IOException { print.e("infer" +
	 * location); return super.inferModuleName(location); }
	 */

	/*
	 * @Override public Iterable<Set<Location>> listLocationsForModules(Location location) throws
	 * IOException { print.e("listLoc" + location); return super.listLocationsForModules(location); }
	 */

	/*
	 * public boolean contains(Location location, FileObject fo) throws IOException { print.e("contains"
	 * + location); return super.contains(location, fo); }
	 */

	@Override
	public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
		// print.e("getJavaFileForOutput:" + location);
		// print.e(className);

		try {
			CompiledCode innerClass = new CompiledCode(className);
			compiledCode.add(innerClass);
			cl.addCode(innerClass);
			return innerClass;
		}
		catch (Exception e) {
			throw new RuntimeException("Error while creating in-memory output file for " + className, e);
		}
	}

	@Override
	public ClassLoader getClassLoader(JavaFileManager.Location location) {
		// print.e("getClassLoader" + location);
		return cl;
	}
}
