component extends="org.lucee.cfml.test.LuceeTestCase" labels="classloader,memory,leak" {

	function beforeAll() {
		variables.testPrefix = "LDEV5903_3_tmp";
		variables.testWorkingDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "/" & variables.testPrefix & "/";
		if ( directoryExists( variables.testWorkingDir ) ) {
			directoryDelete( variables.testWorkingDir, true );
		}
		directoryCreate( variables.testWorkingDir );
		pagePoolClear( force=true );
	}

	function afterAll() {
		if ( directoryExists( variables.testWorkingDir ) ) {
			directoryDelete( variables.testWorkingDir, true );
		}
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
				var initialPagesCleared = PhysicalClassLoader.getLastFlushPagesCleared();
				var initialAllSize = pcl.getSize( true );
				var initialUniqueSize = pcl.getSize( false );

				systemOutput( "LDEV5903_3: initial state - pclHash=#initialPclHash#, lastFlushPagesCleared=#initialPagesCleared#, allSize=#initialAllSize#, uniqueSize=#initialUniqueSize#, classRootDir=#classRootDir#", true );

				// Reset flush stats before test
				PhysicalClassLoader.resetLastFlushPagesCleared();
				systemOutput( "LDEV5903_3: after reset - lastFlushPagesCleared=#PhysicalClassLoader.getLastFlushPagesCleared()#", true );

				// Create and modify component to trigger classloader flushes
				// Default thresholds: count=1000, ratio=3
				// Need >1000 classes with ratio>3 to trigger flush
				var componentPath = variables.testWorkingDir & "_LDEV5903Test.cfc";
				var componentName = variables.testPrefix & "._LDEV5903Test";

				var pclHashesSeen = { "#initialPclHash#": true };
				var maxPagesCleared = 0;
				var flushCount = 0;

				// Strategy: Create and repeatedly modify 5 unique components
				// Each component gets modified many times, accumulating renamed classes
				// With 5 components modified 1000 times each = 5000 renames, ratio = 5000/5 = 1000
				var numComponents = 5;
				var modifyCount = 1000;
				var totalIterations = numComponents * modifyCount;

				loop from=1 to=totalIterations index="local.i" {
					var compIndex = ( ( local.i - 1 ) mod numComponents ) + 1;
					var compPath = variables.testWorkingDir & "_LDEV5903Test#compIndex#.cfc";
					var compName = variables.testPrefix & "._LDEV5903Test#compIndex#";

					fileWrite( compPath, "component { function getVersion() { return '#local.i#'; } }" );
					createObject( "component", compName );

					var currentPcl = PhysicalClassLoaderFactory.getPhysicalClassLoader( config, classRootDir, false );
					var currentPclHash = currentPcl.hashCode();

					// Log progress at intervals
					if ( local.i == 500 || local.i == 1000 || local.i == 2000 || local.i == 3000 ) {
						var currentAllSize = currentPcl.getSize( true );
						var currentUniqueSize = currentPcl.getSize( false );
						var ratio = currentUniqueSize > 0 ? currentAllSize / currentUniqueSize : 0;
						systemOutput( "LDEV5903_3: iteration #local.i# - allSize=#currentAllSize#, uniqueSize=#currentUniqueSize#, ratio=#numberFormat( ratio, '0.00' )#, pclHash=#currentPclHash#", true );
					}

					if ( !structKeyExists( pclHashesSeen, currentPclHash ) ) {
						pclHashesSeen[ currentPclHash ] = true;
						flushCount++;
						// A flush happened - check if pages were cleared
						var lastPagesCleared = PhysicalClassLoader.getLastFlushPagesCleared();
						systemOutput( "LDEV5903_3: flush ###flushCount# at iteration #local.i# - newPclHash=#currentPclHash#, pagesCleared=#lastPagesCleared#", true );
						if ( lastPagesCleared > maxPagesCleared ) {
							maxPagesCleared = lastPagesCleared;
						}
					}
				}

				systemOutput( "LDEV5903_3: final - flushCount=#flushCount#, pclHashesSeen=#structCount( pclHashesSeen )#, maxPagesCleared=#maxPagesCleared#", true );

				// Multiple classloaders means flushes happened
				expect( structCount( pclHashesSeen ) ).toBeGT( 1, "Flush should have created multiple PhysicalClassLoaders, got #structCount( pclHashesSeen )#" );
				// Pages should have been cleared during at least one flush
				expect( maxPagesCleared ).toBeGT( 0, "PageSourcePool.clearPages() should have cleared pages during flush, got #maxPagesCleared#" );
			});
		});
	}
}
