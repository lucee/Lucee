component extends="org.lucee.cfml.test.LuceeTestCase" labels="classloader,memory,leak" {

	function beforeAll() {
		variables.testPrefix = "LDEV5903_4_tmp";
		variables.testWorkingDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "/" & variables.testPrefix & "/";
		if ( directoryExists( variables.testWorkingDir ) ) {
			directoryDelete( variables.testWorkingDir, true );
		}
		directoryCreate( variables.testWorkingDir );
	}

	function afterAll() {
		if ( directoryExists( variables.testWorkingDir ) ) {
			directoryDelete( variables.testWorkingDir, true );
		}
	}

	function run( testResults, testBox ) {
		describe( "LDEV-5903 - Application mapping SoftReference causes repeated class loading", function() {

			it( "static components should not cause class renames when application mapping is evicted", function() {
				var PhysicalClassLoaderFactory = createObject( "java", "lucee.commons.lang.PhysicalClassLoaderFactory" );
				var pageContext = getPageContext();
				var config = pageContext.getConfig();

				// Create a STATIC component (file never changes)
				var componentPath = variables.testWorkingDir & "StaticComponent.cfc";
				var componentName = variables.testPrefix & ".StaticComponent";
				fileWrite( componentPath, "component { function getValue() { return 'static'; } }" );

				// First load - this creates the application mapping and loads the class
				var obj1 = createObject( "component", componentName );
				expect( obj1.getValue() ).toBe( "static" );

				// Get the class name - should NOT have a rename suffix like $1, $2
				var className1 = obj1.getComponentPage().getClass().getName();
				systemOutput( "LDEV5903_4: first load - className=#className1#", true );

				// Get initial classloader state
				var ps = pageContext.getCurrentPageSource();
				var mapping = ps.getMapping();
				var classRootDir = mapping.getClassRootDirectory();
				var pcl = PhysicalClassLoaderFactory.getPhysicalClassLoader( config, classRootDir, false );
				var initialAllSize = pcl.getSize( true );
				var initialUniqueSize = pcl.getSize( false );
				systemOutput( "LDEV5903_4: initial classloader state - all=#initialAllSize#, unique=#initialUniqueSize#", true );

				// Force GC to try to evict SoftReferences (application mappings)
				var System = createObject( "java", "java.lang.System" );
				System.gc();
				sleep( 500 );
				System.gc();
				sleep( 500 );

				// Now load the SAME component again - file hasn't changed
				// If SoftReference was evicted, a new mapping is created with empty PageSourcePool
				// This would cause the class to be "loaded" again, triggering a rename
				var renameCount = 0;
				var classNames = { "#className1#": true };

				loop from=1 to=100 index="local.i" {
					// Force more GC pressure
					if ( local.i mod 10 == 0 ) {
						System.gc();
					}

					var obj = createObject( "component", componentName );
					var className = obj.getComponentPage().getClass().getName();

					if ( !structKeyExists( classNames, className ) ) {
						classNames[ className ] = true;
						renameCount++;
						systemOutput( "LDEV5903_4: iteration #local.i# - NEW className=#className# (rename detected!)", true );
					}
				}

				// Check final classloader state
				pcl = PhysicalClassLoaderFactory.getPhysicalClassLoader( config, classRootDir, false );
				var finalAllSize = pcl.getSize( true );
				var finalUniqueSize = pcl.getSize( false );
				systemOutput( "LDEV5903_4: final classloader state - all=#finalAllSize#, unique=#finalUniqueSize#", true );
				systemOutput( "LDEV5903_4: total unique classNames seen=#structCount( classNames )#, renames=#renameCount#", true );

				// With STATIC files and proper caching, there should be NO renames
				// All 100 iterations should return the exact same class
				expect( renameCount ).toBe( 0, "Static component should not cause class renames, but got #renameCount# renames. ClassNames seen: #structKeyList( classNames )#" );
			});


		});
	}
}
