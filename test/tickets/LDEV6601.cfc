component extends="org.lucee.cfml.test.LuceeTestCase" labels="xml" {

	function run( testResults, testBox ) {
		describe( "LDEV-6601 xmlFeatures alias check compares Boolean values, not instances", function() {

			it( "accepts two distinct java.lang.Boolean(true) instances", function() {
				var a = createObject( "java", "java.lang.Boolean" ).init( true );
				var b = createObject( "java", "java.lang.Boolean" ).init( true );
				var doc = xmlParse( '<?xml version="1.0"?><foo/>', false, {
					"externalGeneralEntities": a,
					"allowExternalEntities": b,
					"secure": false,
					"disallowDoctypeDecl": false
				} );
				expect( doc.xmlRoot.xmlName ).toBe( "foo" );
			});

			it( "accepts two distinct java.lang.Boolean(false) instances", function() {
				var a = createObject( "java", "java.lang.Boolean" ).init( false );
				var b = createObject( "java", "java.lang.Boolean" ).init( false );
				var doc = xmlParse( '<?xml version="1.0"?><foo/>', false, {
					"externalGeneralEntities": a,
					"allowExternalEntities": b
				} );
				expect( doc.xmlRoot.xmlName ).toBe( "foo" );
			});

			it( "still rejects values that don't match", function() {
				expect( function() {
					xmlParse( '<?xml version="1.0"?><foo/>', false, {
						"externalGeneralEntities": true,
						"allowExternalEntities": false
					} );
				}).toThrow();
			});
		});
	}
}
