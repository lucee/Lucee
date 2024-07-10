package lucee.runtime.mvn;

import lucee.print;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourcesImpl;
import lucee.runtime.mvn.MavenUtil.GAVSO;

public class Test {
	public static void main(String[] args) throws Exception {
		Resource dir = ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Tmp3");
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

				new GAVSO("commons-beanutils", "commons-beanutils", "1.9.4"),

				new GAVSO("org.apache.maven.resolver", "maven-resolver-impl", "2.0.0"),

				new GAVSO("jakarta.enterprise", "jakarta.enterprise.cdi-api", "4.0.1"),

				new GAVSO("org.lucee", "lucee", "6.1.0.235-RC")

		};

		arr = new GAVSO[] {

				// new GAVSO("org.apache.maven", "maven-parent", "40"),

				// new GAVSO("org.apache", "apache", "30"),

				// new GAVSO("com.puppycrawl.tools", "checkstyle", "7.8"),

				// new GAVSO("org.apache.commons", "commons-lang3", "3.12.0"),

				// new GAVSO("org.apache.httpcomponents", "httpclient", "4.5.14"),

				// new GAVSO("org.apache.httpcomponents", "httpcomponents-client", "4.5.14"),

				new GAVSO("org.apache.commons", "commons-pool2", "2.12.0"),

				// new GAVSO("org.apache.commons", "commons-parent", "62"),

				new GAVSO("org.slf4j", "slf4j-api", "1.6.1"),

				// new GAVSO("net.bytebuddy", "byte-buddy", "1.14.17"),

				// new GAVSO("net.bytebuddy", "byte-buddy-parent", "1.14.17"),

				new GAVSO("commons-beanutils", "commons-beanutils", "1.9.4"),

				// new GAVSO("org.apache.maven.resolver", "maven-resolver-impl", "2.0.0"),

				new GAVSO("jakarta.enterprise", "jakarta.enterprise.cdi-api", "4.0.1"),

				new GAVSO("org.lucee", "lucee", "6.1.0.235-RC")

		};

		/*
		 * new Artifact("org.hibernate.orm", "hibernate-core", "6.5.2.Final"), new Artifact("com.amazonaws",
		 * "aws-java-sdk-s3", "1.12.756"),
		 * 
		 * new Artifact("org.apache.maven", "maven-plugin-api", "3.9.8"),
		 * 
		 * new Artifact("org.apache.maven", "maven-core", "3.9.8"), // new Artifact(,,),new Artifact(,,),new
		 * Artifact(,,),new Artifact(,,), xception in thread "main" java.io.IOException: cannot resolve
		 * [${sisuVersion}] for [groupID:org.apache.maven;artifactId:maven-parent;version:40], available
		 * properties are [version.apache-rat-plugin, version.maven-help-plugin,
		 * version.maven-source-plugin, distMgmtReleasesUrl, version.maven-plugin-tools,
		 * distMgmtSnapshotsUrl, version.maven-ear-plugin, version.maven-deploy-plugin, surefire.version,
		 * sourceReleaseAssemblyDescriptor, organization.logo, version.maven-compiler-plugin, twitter,
		 * version.maven-shade-plugin, version.maven-gpg-plugin, project.build.sourceEncoding,
		 * version.maven-enforcer-plugin, version.maven-invoker-plugin, distMgmtSnapshotsName,
		 * assembly.tarLongFileMode, version.maven-scm-publish-plugin,
		 * version.maven-remote-resources-plugin, version.maven-war-plugin, version.apache-resource-bundles,
		 * distMgmtReleasesName, minimalJavaBuildVersion, maven.plugin.tools.version,
		 * version.checksum-maven-plugin, version.maven-fluido-skin, maven.compiler.source,
		 * version.maven-assembly-plugin, version.maven-resources-plugin, minimalMavenBuildVersion,
		 * project.reporting.outputEncoding, version.maven-jar-plugin, version.maven-scm-plugin,
		 * maven.compiler.target, version.maven-dependency-plugin, version.maven-clean-plugin,
		 * version.maven-javadoc-plugin, version.maven-site-plugin, project.build.outputTimestamp,
		 * version.maven-release-plugin, gpg.useagent, version.maven-antrun-plugin, version.maven-surefire,
		 * version.maven-install-plugin, version.maven-project-info-reports-plugin]
		 * 
		 * };
		 */
		/*
		 * for (Artifact a: examples) { print.e("------------ " + a); print.e(maven.download(a.groupId,
		 * a.artifactId, a.version, true, false)); }
		 */
		for (GAVSO gav: arr) {
			POM pom = POM.getInstance(dir, gav.g, gav.a, gav.v, POM.SCOPE_NOT_TEST);
			print.e("==========================================");
			print.e(pom.getName());
			print.e(pom);
			print.e("==========================================");

			// print.e("--- properties ---");
			// print.e(pom.getAllParentsAsTree());
			// print.e(pom.getProperties());
			print.e("--- packaging ---");
			print.e(pom.getPackaging());

			print.e("--- parents ---");
			// print.e(pom.getAllParentsAsTree());
			print.e(pom.getAllParents());

			print.e("--- repositories ---");
			// print.e(pom.getAllParentsAsTree());
			print.e(pom.getRepositories());

			print.e("--- dependencies management ---");
			print.e(pom.getDependencyManagement());

			print.e("--- all dependencies management ---");
			print.e(pom.getAllDependencyManagement());

			print.e("--- dependencies ---");
			// print.e(getDependenciesAsTrees(pom, true));
			print.e(pom.getAllDependencies());

			// pom.getScope();
			// print.e(pom.getDependencyManagement());

			// print.e(maven.getDependencies(groupId, artifactId, version, true, false, true));

			break;
		}
	}

}
