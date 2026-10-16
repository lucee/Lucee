component extends="org.lucee.cfml.test.LuceeTestCase" labels="pagesource,hashcode" {

	function run( testResults, testBox ) {
		describe( "LDEV-6072: PageSource hashCode implementation", function() {

			it( "PageSource.equals() and hashCode() must follow Java contract", function() {
				var mapping = getPageContext().getCurrentPageSource().getMapping();

				// Create two PageSource objects for the same path
				var ps1 = mapping.getPageSource( "/test.cfm", false );
				var ps2 = mapping.getPageSource( "/test.cfm", false );

				// If equals returns true, hashCode must return same value
				if ( ps1.equals( ps2 ) ) {
					expect( ps1.hashCode() ).toBe( ps2.hashCode(),
						"PageSource objects that are equal must have the same hashCode" );
				}
			});

			it( "HashSet with PageSource objects should work correctly", function() {
				var mapping = getPageContext().getCurrentPageSource().getMapping();
				var includeOnce = createObject( "java", "java.util.HashSet" ).init();

				// Add first PageSource
				var ps1 = mapping.getPageSource( "/foo.cfm", false );
				includeOnce.add( ps1 );

				expect( includeOnce.size() ).toBe( 1 );
				expect( includeOnce.contains( ps1 ) ).toBeTrue( "HashSet should contain ps1" );

				// Get PageSource again for same path
				var ps2 = mapping.getPageSource( "/foo.cfm", false );

				// HashSet should recognize it as the same (either same instance or equals+hashCode)
				expect( includeOnce.contains( ps2 ) ).toBeTrue(
					"HashSet should recognize ps2 as equivalent to ps1" );

				// Adding again should not increase size
				includeOnce.add( ps2 );
				expect( includeOnce.size() ).toBe( 1, "HashSet should not contain duplicates" );
			});

			it( "Different PageSource paths should have different hashCodes", function() {
				var mapping = getPageContext().getCurrentPageSource().getMapping();

				var ps1 = mapping.getPageSource( "/test1.cfm", false );
				var ps2 = mapping.getPageSource( "/test2.cfm", false );

				// Different paths should not be equal
				expect( ps1.equals( ps2 ) ).toBeFalse( "Different paths should not be equal" );

				// While not strictly required, different objects should ideally have different hashCodes
				// This is a best practice to avoid hash collisions
				if ( ps1.hashCode() == ps2.hashCode() ) {
					// Log a warning but don't fail - hash collisions are allowed but not ideal
					systemOutput( "Warning: Different PageSource paths have same hashCode (collision)", true );
				}
			});

			it( "HashCode bug manifests when pool is cleared between PageSource creations", function() {
				var mapping = getPageContext().getCurrentPageSource().getMapping();
				var includeOnce = createObject( "java", "java.util.HashSet" ).init();

				// Create first PageSource and add to HashSet
				var ps1 = mapping.getPageSource( "/hashcode-test.cfm", false );
				includeOnce.add( ps1 );

				expect( includeOnce.size() ).toBe( 1 );
				expect( includeOnce.contains( ps1 ) ).toBeTrue();

				// Clear the page pool to force new PageSource creation
				pagePoolClear( force=true );

				// Get PageSource again for same path - this will create a NEW instance
				var ps2 = mapping.getPageSource( "/hashcode-test.cfm", false );

				// They should be equal (same path)
				expect( ps1.equals( ps2 ) ).toBeTrue( "Same path should be equal" );

				// THIS IS THE BUG: Without proper hashCode(), HashSet won't find ps2
				// even though ps1.equals(ps2) is true
				expect( includeOnce.contains( ps2 ) ).toBeTrue(
					"HashSet should recognize ps2 as equivalent to ps1 (requires proper hashCode)" );

				// Verify they have same hashCode (will fail without fix)
				expect( ps1.hashCode() ).toBe( ps2.hashCode(),
					"Equal PageSource objects must have same hashCode" );
			});

			it( "Components with same name in different packages should have different hashCodes", function() {
				// Create test components in different directories
				var baseDir = getTempDirectory() & "LDEV6072/";
				directoryCreate( baseDir & "pkg1", true );
				directoryCreate( baseDir & "pkg2", true );

				fileWrite( baseDir & "pkg1/Test.cfc", "component {}" );
				fileWrite( baseDir & "pkg2/Test.cfc", "component {}" );

				try {
					var mapping = getPageContext().getCurrentPageSource().getMapping();

					var ps1 = mapping.getPageSource( "/LDEV6072/pkg1/Test.cfc", false );
					var ps2 = mapping.getPageSource( "/LDEV6072/pkg2/Test.cfc", false );

					// Different packages should not be equal (LDEV-6056 fix)
					expect( ps1.equals( ps2 ) ).toBeFalse(
						"Components with same name in different packages should not be equal" );

					// Should have different hashCodes
					expect( ps1.hashCode() ).notToBe( ps2.hashCode(),
						"Components with same name in different packages should have different hashCodes" );
				}
				finally {
					directoryDelete( baseDir, true );
				}
			});

		});
	}

}
