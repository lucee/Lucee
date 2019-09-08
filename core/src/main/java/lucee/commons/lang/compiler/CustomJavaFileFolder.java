package lucee.commons.lang.compiler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;

public class CustomJavaFileFolder {
	private final List<JavaFileObject> elements = new ArrayList<JavaFileObject>();
	private final BundleContext context;
	private String packageName;

	public CustomJavaFileFolder(BundleContext context, String packageName) throws IOException, URISyntaxException {
		this.context = context;
		this.packageName = packageName;
		elements.addAll(findAll(packageName));
	}

	public List<JavaFileObject> getEntries() {
		return Collections.unmodifiableList(elements);
	}

	public String getPackageName() {
		return packageName;
	}

	private List<JavaFileObject> findAll(String packageName) throws IOException, URISyntaxException {

		List<JavaFileObject> result;
		packageName = packageName.replaceAll("\\.", "/");

		result = new ArrayList<JavaFileObject>();
		Bundle[] b = context.getBundles();
		for (Bundle bundle: b) {
			enumerateWiring(packageName, result, bundle);
		}
		return result;
	}

	private void enumerateWiring(String packageName, List<JavaFileObject> result, Bundle b) throws URISyntaxException, IOException {
		BundleWiring bw = b.adapt(BundleWiring.class);
		Collection<String> cc = bw.listResources(packageName, null, BundleWiring.LISTRESOURCES_RECURSE);
		for (String resource: cc) {
			URL u = b.getResource(resource);
			if (u != null) {
				InputStream openStream = null;
				try {
					openStream = u.openStream();
					final CustomJavaFileObject customJavaFileObject = new CustomJavaFileObject(resource, u.toURI(), openStream, Kind.CLASS);
					result.add(customJavaFileObject);
				}
				catch (FileNotFoundException e) {
					final CustomJavaFileObject customJavaFileObject = new CustomJavaFileObject(resource, u.toURI(), null, Kind.CLASS);
					result.add(customJavaFileObject);
				}
			}
		}
	}
}