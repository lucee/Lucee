component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, textbox ) {
		describe("URLClassLoader Method Caching Cross-ClassLoader Conversion Issue", function() {

			it("should handle method caching with objects from different classloaders without NPE", function() {
				// Test case for regression where method caching fails when objects from
				// different URLClassLoaders are used due to cross-classloader type conversion failures
				var tempDir = getTempDirectory( unique="LDEV5807" );

				// Test with three different versions to ensure different classloaders
				var versions = [ "3.16.0", "3.17.0", "3.18.0" ];

				loop array=versions item="local.version" index="local.i" {
					var testName = "Call ##" & local.i & " (v" & version & ")";
					createURLClassLoaderAndTest( tempDir, version, testName );
				}
			});
		});
	}

	private function downloadJarFromMaven(groupId, artifactId, version, localDir) {
		var name = artifactId & "-" & version & ".jar";
		var mavenUrl = "https://repo1.maven.org/maven2/" &
					  replace(groupId, ".", "/", "all") & "/" &
					  artifactId & "/" & version & "/" &
					  name;

		var jarFile = localDir & "/" & name;
		// Download if not exists
		if (!fileExists(jarFile)) {
			fileCopy(mavenUrl, jarFile);
		}

		return jarFile;
	}

	private function createURLClassLoaderAndTest(localdir, version, testName) {
		var jarPath = downloadJarFromMaven("org.apache.commons", "commons-lang3", version, localdir);

		// Create new URLClassLoader each time (different classloader per version)
		var file = new java:java.io.File(jarPath);
		var urls = [file.toURI().toURL()];
		var classLoader = new java:java.net.URLClassLoader(urls);

		// Load classes from THIS specific classloader
		var styleClass = classLoader.loadClass("org.apache.commons.lang3.builder.RecursiveToStringStyle");
		var style = styleClass.newInstance();

		var builderClass = classLoader.loadClass("org.apache.commons.lang3.builder.ToStringBuilder");
		var builder = builderClass.getConstructor([new java:java.lang.Object().getClass()])
									   .newInstance(["test object from " & testName]);

		// This is the critical call that triggers the caching/conversion issue:
		// - First call: caches setDefaultStyle method with RecursiveToStringStyle from classloader 1
		// - Second call: cache hit, but tries to convert RecursiveToStringStyle from classloader 2
		//   using types from classloader 1 - this would fail without the fix
		// - With fix: PageException caught, falls back to normal method lookup
		builder.setDefaultStyle(style);

		// Additional method call to further test caching
		var result = builder.toString();

		// If we get here without exception, the fix is working
		return true;
	}
}