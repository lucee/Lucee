component extends="org.lucee.cfml.test.LuceeTestCase" labels="classloader,memory,leak" {

	function beforeAll() {
		variables.testPrefix = "_LDEV5903_psp_tmp";
		variables.curr = getDirectoryFromPath( getCurrentTemplatePath() ) & "/" & variables.testPrefix & "/";
		if ( directoryExists( variables.curr ) ) {
			directoryDelete( variables.curr, true );
		}
		directoryCreate( variables.curr );
		pagePoolClear( force=true );
	}

	function afterAll() {
		// leave artifacts for inspection
	}

	function run( testResults, testBox ) {
		describe( "LDEV-5903 - PageSourcePool leak when classloader flushes", function() {

			it( "PageSourcePool.clearPages() should be called when classloader is flushed", function() {
				// Get the mapping's PhysicalClassLoader
				var pageContext = getPageContext();
				var ps = pageContext.getCurrentPageSource();
				var mapping = ps.getMapping();
				var PhysicalClassLoader = createObject( "java", "lucee.commons.lang.PhysicalClassLoader" );
				var PhysicalClassLoaderFactory = createObject( "java", "lucee.commons.lang.PhysicalClassLoaderFactory" );
				var config = pageContext.getConfig();
				var classRootDir = mapping.getClassRootDirectory();

				var pcl = PhysicalClassLoaderFactory.getPhysicalClassLoader( config, classRootDir, false );
				var initialPclHash = pcl.hashCode();

				// Reset flush stats before test
				PhysicalClassLoader.resetLastFlushPagesCleared();

				// Create and modify component to trigger classloader flushes
				// Default thresholds: count=1000, ratio=3
				// Need >1000 classes with ratio>3 to trigger flush
				var componentPath = variables.curr & "_LDEV5903Test.cfc";
				var componentName = variables.testPrefix & "._LDEV5903Test";

				var pclHashesSeen = { "#initialPclHash#": true };
				var maxPagesCleared = 0;

				// 3500 iterations with 1 unique class = ratio of 3500 (way over 3)
				loop from=1 to=3500 index="local.i" {
					fileWrite( componentPath, "component { function getVersion() { return '#local.i#'; } }" );
					createObject( "component", componentName );

					var currentPcl = PhysicalClassLoaderFactory.getPhysicalClassLoader( config, classRootDir, false );
					var currentPclHash = currentPcl.hashCode();

					if ( !structKeyExists( pclHashesSeen, currentPclHash ) ) {
						pclHashesSeen[ currentPclHash ] = true;
						// A flush happened - check if pages were cleared
						var lastPagesCleared = PhysicalClassLoader.getLastFlushPagesCleared();
						if ( lastPagesCleared > maxPagesCleared ) {
							maxPagesCleared = lastPagesCleared;
						}
					}
				}

				// Multiple classloaders means flushes happened
				expect( structCount( pclHashesSeen ) ).toBeGT( 1, "Flush should have created multiple PhysicalClassLoaders" );
				// Pages should have been cleared during at least one flush
				expect( maxPagesCleared ).toBeGT( 0, "PageSourcePool.clearPages() should have cleared pages during flush" );
			});
		});
	}
}
