component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( title="LDEV-6480 cfproperty default for numeric and boolean return string", body=function() {

			it( title="numeric expression default should return numeric", body=function( currentSpec ) {
				var comp = new LDEV6480.ScriptDefaults();
				expect( comp.getNumExpr() ).toBe( 30 );
				expect( serializeJSON( comp.getNumExpr() ) ).toBe( "30" );
			});

			it( title="boolean expression default should return boolean", body=function( currentSpec ) {
				var comp = new LDEV6480.ScriptDefaults();
				expect( comp.getBoolExpr() ).toBeFalse();
				expect( serializeJSON( comp.getBoolExpr() ) ).toBe( "false" );
			});

			it( title="complex expression default should return array", body=function( currentSpec ) {
				var comp = new LDEV6480.ComplexDefault();
				expect( comp.getArrExpr() ).toBeArray();
				expect( comp.getArrExpr() ).toHaveLength( 0 );
			});

			it( title="cfproperty tag expression defaults should keep their type", body=function( currentSpec ) {
				var comp = new LDEV6480.TagDefaults();
				expect( serializeJSON( comp.getNumExpr() ) ).toBe( "30" );
				expect( serializeJSON( comp.getBoolExpr() ) ).toBe( "true" );
			});

			it( title="literal defaults should stay as string", body=function( currentSpec ) {
				var comp = new LDEV6480.ScriptDefaults();
				expect( serializeJSON( comp.getNumLiteral() ) ).toBe( '"30"' );
				expect( serializeJSON( comp.getBoolLiteral() ) ).toBe( '"false"' );
				expect( serializeJSON( comp.getStrLiteral() ) ).toBe( '"lucee"' );
				expect( serializeJSON( new LDEV6480.TagDefaults().getNumLiteral() ) ).toBe( '"30"' );
			});

		});
	}

}
