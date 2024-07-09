package lucee.runtime.mvn;

import lucee.print;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourcesImpl;

public class Test {
	public static void main(String[] args) throws Exception {
		Resource dir = ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Tmp3");

		String groupId = "org.apache.maven";
		String artifactId = "maven-parent";
		String version = "40";

		groupId = "org.apache";
		artifactId = "apache";
		version = "30";

		groupId = "com.puppycrawl.tools";
		artifactId = "checkstyle";
		version = "7.8";

		groupId = "org.apache.commons";
		artifactId = "commons-lang3";
		version = "3.12.0";

		groupId = "org.apache.httpcomponents";
		artifactId = "httpclient";
		version = "4.5.14";

		groupId = "org.apache.httpcomponents";
		artifactId = "httpcomponents-client";
		version = "4.5.14";

		groupId = "org.apache.commons";
		artifactId = "commons-pool2";
		version = "2.12.0";

		groupId = "org.apache.commons";
		artifactId = "commons-parent";
		version = "62";

		groupId = "org.slf4j";
		artifactId = "slf4j-api";
		version = "1.6.1";

		groupId = "net.bytebuddy";
		artifactId = "byte-buddy";
		version = "1.14.17";

		// groupId = "net.bytebuddy";
		// artifactId = "byte-buddy-parent";
		// version = "1.14.17";

		groupId = "org.lucee";
		artifactId = "lucee";
		version = "6.1.0.235-RC";
		groupId = "org.apache.maven.resolver";
		artifactId = "maven-resolver-impl";
		version = "2.0.0";
		groupId = "commons-beanutils";
		artifactId = "commons-beanutils";
		version = "1.9.4";

		// groupId = "xerces";
		// artifactId = "xerces-impl";
		// version = "2.6.2";

		/*
		 * Artifact[] examples = new Artifact[] {
		 * 
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

		POM pom = POM.getInstance(dir, groupId, artifactId, version, POM.SCOPE_NOT_TEST);
		print.e("==========================================");
		print.e(pom);
		print.e("==========================================");

		// print.e("--- properties ---");
		// print.e(pom.getAllParentsAsTree());
		// print.e(pom.getProperties());

		print.e("--- parents ---");
		// print.e(pom.getAllParentsAsTree());
		print.e(pom.getAllParents());

		print.e("--- repositories ---");
		// print.e(pom.getAllParentsAsTree());
		print.e(pom.getRepositories());

		print.e("--- dependencies ---");
		// print.e(getDependenciesAsTrees(pom, true));
		print.e(pom.getAllDependencies());

		// pom.getScope();

		print.e("--- dependencies management ---");
		print.e(pom.getAllDependencyManagement());
		// print.e(pom.getDependencyManagement());

		// print.e(maven.getDependencies(groupId, artifactId, version, true, false, true));

	}

}
