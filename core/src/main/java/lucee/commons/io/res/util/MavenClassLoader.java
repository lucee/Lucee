package lucee.commons.io.res.util;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lucee.runtime.mvn.POM;

public class MavenClassLoader extends ResourceClassLoader {

	private static Map<String, MavenClassLoader> instances = new ConcurrentHashMap<>();

	public MavenClassLoader(POM pom, ClassLoader parent) throws IOException {
		super(pom.getJars(), parent);
	}

	public static MavenClassLoader getInstance(POM pom, ClassLoader parent) throws IOException {
		MavenClassLoader mcl = instances.get(pom.hash());
		if (mcl == null) {
			mcl = new MavenClassLoader(pom, parent);
			instances.put(pom.hash(), mcl);
		}
		return mcl;
	}
}
