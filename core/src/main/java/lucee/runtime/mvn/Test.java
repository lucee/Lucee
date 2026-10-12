package lucee.runtime.mvn;

import java.util.Map.Entry;

import lucee.aprint;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.mvn.MavenUtil.GAVSO;

public final class Test {
	public static void main(String[] args) throws Exception {

		for (Entry<Object, Object> e: System.getProperties().entrySet()) {
			aprint.e(e.getKey() + ":" + e.getValue());
		}

		Resource dir = ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Tmp3/www");
		GAVSO[] arr = new GAVSO[] {

				new GAVSO("org.apache.maven", "maven-parent", "40"),

				new GAVSO("org.apache", "apache", "30"),

				new GAVSO("com.puppycrawl.tools", "checkstyle", "7.8"),

				new GAVSO("org.apache.commons", "commons-lang3", "3.12.0"),

				new GAVSO("org.apache.httpcomponents", "httpclient", "4.5.14"),

				new GAVSO("org.apache.httpcomponents", "httpcomponents-client", "4.5.14"),

				new GAVSO("org.apache.commons", "commons-pool2", "2.12.0"),

				new GAVSO("org.apache.commons", "commons-parent", "62"),

				new GAVSO("org.slf4j", "slf4j-api", "1.6.1"),

				new GAVSO("net.bytebuddy", "byte-buddy", "1.14.17"),

				new GAVSO("net.bytebuddy", "byte-buddy-parent", "1.14.17"),

				new GAVSO("commons-beanutils", "commons-beanutils", "1.9.4", null, null, "sha1:d52b9abcd97f38c81342bb7e7ae1eee9b73cba51"),

				new GAVSO("org.apache.maven.resolver", "maven-resolver-impl", "2.0.0"),

				new GAVSO("jakarta.enterprise", "jakarta.enterprise.cdi-api", "4.0.1"),

				new GAVSO("org.lucee", "lucee", "6.1.0.235-RC"),

				new GAVSO("com.github.tjake", "jlama-core", "0.7.0"),

				new GAVSO("org.jboss.ejb3", "jboss-ejb3-api", "3.1.0"),

				new GAVSO("org.eclipse.sisu", "org.eclipse.sisu.plexus", "0.3.4"),

				new GAVSO("org.apache.commons", "commons-jexl3", "3.4.0"),

				new GAVSO("commons-beanutils", "commons-beanutils", "1.9.4")

		};

		arr = new GAVSO[] {
				// groupID:com.github.tjake;artifactId:jlama-core;version:0.7.0

				// new GAVSO("com.github.tjake", "jlama-core", "0.7.0")
				// new GAVSO("org.jboss.ejb3", "jboss-ejb3-api", "3.1.0")
				new GAVSO("org.eclipse.sisu", "org.eclipse.sisu.plexus", "0.3.4")
				// new GAVSO("commons-beanutils", "commons-beanutils", "1.9.4", null, null,
				// "sha1:d52b9abcd97f38c81342bb7e7ae1eee9b73cba51")
				// new GAVSO("org.apache.commons", "commons-jexl3", "3.4.0")

		};
		arr = new GAVSO[] {
				// groupID:com.github.tjake;artifactId:jlama-core;version:0.7.0

				// new GAVSO("com.github.tjake", "jlama-core", "0.7.0")
				// new GAVSO("org.jboss.ejb3", "jboss-ejb3-api", "3.1.0")
				new GAVSO("com.github.tjake", "jlama-core", "0.7.0")
				// new GAVSO("commons-beanutils", "commons-beanutils", "1.9.4", null, null,
				// "sha1:d52b9abcd97f38c81342bb7e7ae1eee9b73cba51")
				// new GAVSO("org.apache.commons", "commons-jexl3", "3.4.0")

		};
		arr = new GAVSO[] {
				// groupID:com.github.tjake;artifactId:jlama-core;version:0.7.0

				// new GAVSO("com.github.tjake", "jlama-core", "0.7.0")
				// new GAVSO("org.jboss.ejb3", "jboss-ejb3-api", "3.1.0")
				new GAVSO("org.apache.pdfbox", "pdfbox", "3.0.3"), new GAVSO("org.apache.logging.log4j", "log4j-api", "2.20.0"),
				new GAVSO("org.apache.logging.log4j", "log4j-core", "2.20.0")
				// new GAVSO("commons-beanutils", "commons-beanutils", "1.9.4", null, null,
				// "sha1:d52b9abcd97f38c81342bb7e7ae1eee9b73cba51")
				// new GAVSO("org.apache.commons", "commons-jexl3", "3.4.0")

		};
		arr = new GAVSO[] {
				// groupID:com.github.tjake;artifactId:jlama-core;version:0.7.0

				// new GAVSO("com.github.tjake", "jlama-core", "0.7.0")
				// new GAVSO("org.jboss.ejb3", "jboss-ejb3-api", "3.1.0")
				new GAVSO("org.apache.pdfbox", "pdfbox", "3.0.3")
				// new GAVSO("commons-beanutils", "commons-beanutils", "1.9.4", null, null,
				// "sha1:d52b9abcd97f38c81342bb7e7ae1eee9b73cba51")
				// new GAVSO("org.apache.commons", "commons-jexl3", "3.4.0")

		};

		arr = new GAVSO[] {
				// groupID:com.github.tjake;artifactId:jlama-core;version:0.7.0

				// new GAVSO("com.github.tjake", "jlama-core", "0.7.0")
				// new GAVSO("org.jboss.ejb3", "jboss-ejb3-api", "3.1.0")
				new GAVSO("org.apache.maven", "maven-core", "3.8.1")
				// new GAVSO("commons-beanutils", "commons-beanutils", "1.9.4", null, null,
				// "sha1:d52b9abcd97f38c81342bb7e7ae1eee9b73cba51")
				// new GAVSO("org.apache.commons", "commons-jexl3", "3.4.0")

		};
		arr = new GAVSO[] {
				// groupID:com.github.tjake;artifactId:jlama-core;version:0.7.0

				// new GAVSO("com.github.tjake", "jlama-core", "0.7.0")
				// new GAVSO("org.jboss.ejb3", "jboss-ejb3-api", "3.1.0")
				new GAVSO("com.google.inject", "guice", "4.2.1")
				// new GAVSO("commons-beanutils", "commons-beanutils", "1.9.4", null, null,
				// "sha1:d52b9abcd97f38c81342bb7e7ae1eee9b73cba51")
				// new GAVSO("org.apache.commons", "commons-jexl3", "3.4.0")

		};

		long start = System.currentTimeMillis();
		ResourceUtil.deleteContent(dir, null);
		for (GAVSO gav: arr) {
			POM pom = POM.getInstance(dir, null, gav.g, gav.a, gav.v, null, null, gav.checksum, POM.SCOPE_NOT_TEST, POM.SCOPE_ALL, true, null);

			aprint.e("==========================================");
			aprint.e(pom.getName());
			aprint.e(pom.getChecksum());
			aprint.e(pom);
			aprint.e("==========================================");

			aprint.e("--- jars ---");
			// print.e(getDependenciesAsTrees(pom, true));
			aprint.e(pom.getJarPOMs(true));

			if (true) break;

			// print.e("--- properties ---");
			// print.e(pom.getAllParentsAsTree());
			// print.e(pom.getProperties());
			aprint.e("--- packaging ---");
			aprint.e(pom.getPackaging());

			aprint.e("--- path ---");
			aprint.e(pom.getPath());
			aprint.e("--- hash ---");
			aprint.e(pom.hash());

			aprint.e("--- artifact ---");
			aprint.e(pom.getArtifact());

			aprint.e("--- parents ---");
			// print.e(pom.getAllParentsAsTree());
			aprint.e(pom.getAllParents());

			aprint.e("--- repositories ---");
			// print.e(pom.getAllParentsAsTree());
			aprint.e(pom.getRepositories());

			aprint.e("--- dependencies ---");
			// print.e(getDependenciesAsTrees(pom, true));
			aprint.e(pom.getAllDependencies(true));

			aprint.e("--- jars ---");
			// print.e(getDependenciesAsTrees(pom, true));
			aprint.e(pom.getJars());
			aprint.e(System.currentTimeMillis() - start);

			// aprint.e("--- dependencies management ---");
			// aprint.e(pom.getDependencyManagement());

			// aprint.e("--- all dependencies management ---");
			// aprint.e(pom.getAllDependencyManagement());

			// pom.getScope();
			// print.e(pom.getDependencyManagement());

			// print.e(maven.getDependencies(groupId, artifactId, version, true, false, true));

			break;
		}
	}

}
