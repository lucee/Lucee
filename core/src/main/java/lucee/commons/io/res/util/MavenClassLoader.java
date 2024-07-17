package lucee.commons.io.res.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lucee.runtime.mvn.POM;

public class MavenClassLoader extends ResourceClassLoader {

	private static Map<String, MavenClassLoader> instances = new ConcurrentHashMap<>();
	private POM pom;

	public MavenClassLoader(POM pom, ClassLoader parent) throws IOException {
		super(pom.getJars(), parent);
		this.pom = pom;
	}

	public static MavenClassLoader getInstance(POM pom, ClassLoader parent) throws IOException {
		MavenClassLoader mcl = instances.get(pom.hash());
		if (mcl == null) {
			mcl = new MavenClassLoader(pom, parent);
			instances.put(pom.hash(), mcl);
		}
		return mcl;
	}

	public static MavenClassLoader getInstance(POM[] poms, ClassLoader parent) throws IOException {
		if (poms == null || poms.length == 0) throw new IOException("you need to define at least one POM.");

		Arrays.sort(poms, (pom1, pom2) -> pom1.id().compareTo(pom2.id()));

		for (POM pom: poms) {
			parent = getInstance(pom, parent);
		}
		return (MavenClassLoader) parent;
	}

	public POM getPOM() {
		return pom;
	}
}
