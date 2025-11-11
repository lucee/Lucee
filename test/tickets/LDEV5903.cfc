component extends="org.lucee.cfml.test.LuceeTestCase" labels="classloader,memory" {

	function run( testResults, testBox ) {
		describe( "LDEV-5903 - Per-class classloader regression (memory leak)", function() {

			it( "should create separate classloaders for each template in same directory", function() {
				// Load multiple templates from the same physical directory
				// Each should get its own classloader per LDEV-4739 fix
				var template1 = new LDEV5903.Template1();
				var template2 = new LDEV5903.Template2();
				var template3 = new LDEV5903.Template3();

				// Access the mapping to check classloader count
				var pc = getPageContext();
				var ps = pc.getCurrentPageSource();
				var mapping = ps.getMapping();

				// Get the classloader count - should be 3 (one per template)
				var classLoaderCount = mapping.getClassLoaderCount();

				systemOutput( "ClassLoader count in mapping: #classLoaderCount#", true );

				// With the fix (per-class classloaders), we should have 3 separate classloaders
				// With the regression (per-directory classloaders), we would have 1 classloader
				// Note: There may be additional classloaders from other tests, so check >= 3
				expect( classLoaderCount ).toBeGTE( 3, "Should have at least 3 classloaders (one per template)" );
			});

			it( "should track classloaders per className in MappingImpl", function() {
				// This test will verify the internal structure once we apply the fix
				// We need to check that MappingImpl has a Map<String, PhysicalClassLoaderReference>

				var template1 = new LDEV5903.Template1();
				var template2 = new LDEV5903.Template2();

				// Get the mapping via reflection
				var pageContext = getPageContext();
				var ps = pageContext.getCurrentPageSource();
				var mapping = ps.getMapping();

				systemOutput( "Mapping class: #mapping.getClass().getName()#", true );

				// After the fix, we should be able to verify that loaders map exists
				// For now, just verify the mapping is a MappingImpl
				expect( mapping.getClass().getName() ).toBe( "lucee.runtime.MappingImpl" );
			});

			it( "templates can be loaded and executed multiple times", function() {
				// Verify basic functionality still works
				loop from=1 to=10 index="local.i" {
					var t = new LDEV5903.Template1();
					expect( t.getValue() ).toBe( "template1" );
				}
				
				loop from=1 to=10 index="local.i" {
					var t = new LDEV5903.Template2();
					expect( t.getValue() ).toBe( "template2" );
				}
			});
		});
	}
}
