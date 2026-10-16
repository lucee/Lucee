component extends="org.lucee.cfml.test.LuceeTestCase" {

	// cfproperty default values — per-new freshness, cross-instance isolation, tag/script parity.
	// Distinct from Duplicate.cfc which covers "user mutations survive duplicate".

	function run( testResults, testBox ){

		describe( "expression-form property defaults re-fire on every new", function(){

			it( "createUUID() default produces distinct values for two new instances", function(){
				var a = new property.ExprDefaults();
				var b = new property.ExprDefaults();
				expect( a.getUuidDef() ).notToBe( b.getUuidDef() );
			});

			it( "now() default produces fresh timestamps across two new instances", function(){
				var a = new property.ExprDefaults();
				sleep( 50 );
				var b = new property.ExprDefaults();
				// raw long comparison sidesteps dateCompare's 1-second granularity
				expect( a.getNowDef().getTime() ).toBeLT( b.getNowDef().getTime() );
			});

			it( "100 sequential new instances produce 100 distinct UUIDs", function(){
				var uuids = {};
				for ( var i=1; i<=100; i++ ) {
					uuids[ ( new property.ExprDefaults() ).getUuidDef() ] = true;
				}
				expect( structCount( uuids ) ).toBe( 100 );
			});

		});

		describe( "mutable expression-form defaults are per-instance (the LDEV-6303 7.0 leak contract)", function(){

			it( "struct default is a separate instance per new — mutating one does not leak to sibling", function(){
				var a = new property.ExprDefaults();
				var b = new property.ExprDefaults();
				a.getNowStructDef().n = 999;
				expect( b.getNowStructDef().n ).toBe( 1 );
			});

			it( "struct default — adding a key on one instance does not leak to sibling", function(){
				var a = new property.ExprDefaults();
				var b = new property.ExprDefaults();
				a.getNowStructDef().leakedKey = "should-only-be-on-a";
				expect( structKeyExists( b.getNowStructDef(), "leakedKey" ) ).toBeFalse();
			});

			it( "struct default identity — getNowStructDef() returns the same struct on repeated calls within one instance", function(){
				var a = new property.ExprDefaults();
				a.getNowStructDef().n = 999;
				expect( a.getNowStructDef().n ).toBe( 999 );
			});

		});

		describe( "literal-form property defaults are consistent across instances", function(){

			it( "literal default returns the same value for two new instances", function(){
				var a = new property.ExprDefaults();
				var b = new property.ExprDefaults();
				expect( a.getLiteralDef() ).toBe( b.getLiteralDef() );
				expect( a.getLiteralDef() ).toBe( "construction-literal" );
			});

			it( "setting literal property on one instance does not leak to sibling", function(){
				var a = new property.ExprDefaults();
				var b = new property.ExprDefaults();
				a.setLiteralDef( "modified-on-a" );
				expect( b.getLiteralDef() ).toBe( "construction-literal" );
			});

		});

		describe( "tag-syntax cfproperty parity — same semantics as script syntax", function(){

			it( "tag createUUID() default produces distinct values for two new instances", function(){
				var a = new property.TagExprDefaults();
				var b = new property.TagExprDefaults();
				expect( a.getUuidDef() ).notToBe( b.getUuidDef() );
			});

			it( "tag now() default produces fresh timestamps across two new instances", function(){
				var a = new property.TagExprDefaults();
				sleep( 50 );
				var b = new property.TagExprDefaults();
				expect( a.getNowDef().getTime() ).toBeLT( b.getNowDef().getTime() );
			});

			it( "tag struct default is a separate instance per new — mutating one does not leak", function(){
				var a = new property.TagExprDefaults();
				var b = new property.TagExprDefaults();
				a.getNowStructDef().n = 999;
				expect( b.getNowStructDef().n ).toBe( 1 );
			});

			it( "tag literal default returns the same value for two new instances", function(){
				var a = new property.TagExprDefaults();
				var b = new property.TagExprDefaults();
				expect( a.getLiteralDef() ).toBe( "construction-literal" );
				expect( b.getLiteralDef() ).toBe( "construction-literal" );
			});

		});

	}

}
