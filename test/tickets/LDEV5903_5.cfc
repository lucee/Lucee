component extends="org.lucee.cfml.test.LuceeTestCase" labels="classloader,memory,leak" {

	function run( testResults, testBox ) {
		describe( "LDEV-5903 - Multiple RPC classloaders created across requests", function() {

			it( "should reuse RPC classloader across multiple requests with same loadPaths", function() {
				var PhysicalClassLoaderFactory = createObject( "java", "lucee.commons.lang.PhysicalClassLoaderFactory" );

				// Get initial classloader count via reflection
				var factoryClass = PhysicalClassLoaderFactory.getClass();
				var classLoadersField = factoryClass.getDeclaredField( "classLoaders" );
				classLoadersField.setAccessible( true );
				var classLoadersMap = classLoadersField.get( javacast( "null", 0 ) );
				var initialCount = classLoadersMap.size();

				systemOutput( "LDEV5903_5: Initial RPC classloader count: #initialCount#", true );

				// Make multiple internal requests - this mimics production where each request
				// might call createObject with the same loadPaths (like QR code generation)
				// Need to actually reproduce the bug - 149 classloaders suggests something
				// about the loadPaths array or parent classloader is changing each request
				var iterations = 150;
				var workerPath = "/test/tickets/LDEV5903_5/worker.cfm";
				loop from=1 to=iterations index="local.i" {
					internalRequest(
						template: workerPath,
						method: "GET"
					);

					if ( local.i mod 10 == 0 ) {
						var currentCount = classLoadersMap.size();
						systemOutput( "LDEV5903_5: After #local.i# requests - RPC classloader count: #currentCount#", true );
					}
				}

				var finalCount = classLoadersMap.size();
				var newClassLoaders = finalCount - initialCount;

				systemOutput( "LDEV5903_5: Final RPC classloader count: #finalCount# (created #newClassLoaders# new)", true );

				// We should only create ONE new RPC classloader for these libs across all requests
				// Allow for maybe 2-3 due to test framework overhead, but NOT 100
				expect( newClassLoaders ).toBeLT( 10, "Should not create #newClassLoaders# RPC classloaders across #iterations# requests. Expected < 10" );
			});
		});
	}
}
