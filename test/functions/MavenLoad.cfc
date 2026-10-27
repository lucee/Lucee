component extends="org.lucee.cfml.test.LuceeTestCase" {
	public function run( testResults, testBox ) {
		describe( title="Testcase for MavenLoad() function", body=function() {
			
			it(title="load com.github.tjake", body=function( currentSpec ) {
				var jars=mavenLoad([
					"com.github.tjake:jlama-core:0.7.0",
					"com.github.tjake:jlama-native:0.7.0"
				]);
				// LDEV-6747 only one version per groupId:artifactId (nearest wins), before every conflicting version was loaded
				expect(len(jars)).toBe(28);
				var artifacts={};
				loop array=jars item="local.jar" {
					// {mvnDir}/{group}/{artifact}/{version}/{artifact}-{version}.jar
					var artifactDir=getDirectoryFromPath(getDirectoryFromPath(jar).reReplace("[\\/]$",""));
					expect(structKeyExists(artifacts,artifactDir)).toBeFalse("more than one version of [#artifactDir#]");
					artifacts[artifactDir]=true;
				}
			});

			it(title="load org.apache.commons", body=function( currentSpec ) {
				var l=len(mavenLoad([
					{
						"groupId" : "org.apache.commons",
						"artifactId" : "commons-lang3",
						"version" : "3.12.0"
					}
				]));
				expect(l).toBe(1);
			});

			it(title="load com.google.guava", body=function( currentSpec ) {
				var l=len(mavenLoad([
					{
						"groupId" : "com.google.guava",
						"artifactId" : "guava",
						"version" : "31.0.1-jre"
					}
				]));
				expect(l).toBe(7);
			});

			it(title="load org.slf4j", body=function( currentSpec ) {
				var l=len(mavenLoad([
					{
						"groupId" : "org.slf4j",
						"artifactId" : "slf4j-api",
						"version" : "1.7.32"
					}
				]));
				expect(l).toBe(1);
			});

			it(title="load junit", body=function( currentSpec ) {
				var l=len(mavenLoad([
					{
						"groupId" : "junit",
						"artifactId" : "junit",
						"version" : "4.13.2"
					}
				]));
				expect(l).toBe(2);
			});

			it(title="load org.apache.httpcomponents", body=function( currentSpec ) {
				var l=len(mavenLoad([
					{
						"groupId" : "org.apache.httpcomponents",
						"artifactId" : "httpclient",
						"version" : "4.5.13"
					}
				]));
				expect(l).toBe(4);
			});

			it(title="load ch.qos.logback", body=function( currentSpec ) {
				var l=len(mavenLoad([
					{
						"groupId" : "ch.qos.logback",
						"artifactId" : "logback-classic",
						"version" : "1.2.11"
					}
				]));
				expect(l).toBe(3);
			});

			it(title="load org.hamcrest", body=function( currentSpec ) {
				var l=len(mavenLoad([
					{
						"groupId" : "org.hamcrest",
						"artifactId" : "hamcrest",
						"version" : "2.2"
					}
				]));
				expect(l).toBe(1);
			});

			it(title="load com.fasterxml.jackson.core", body=function( currentSpec ) {
				var l=len(mavenLoad([
					{
						"groupId" : "com.fasterxml.jackson.core",
						"artifactId" : "jackson-databind",
						"version" : "2.12.5"
					}
				]));
				expect(l).toBe(3);
			});

			it(title="load commons-io", body=function( currentSpec ) {
				var l=len(mavenLoad([
					{
						"groupId" : "commons-io",
						"artifactId" : "commons-io",
						"version" : "2.8.0"
					}
				]));
				expect(l).toBe(1);
			});

		});
	}
}
