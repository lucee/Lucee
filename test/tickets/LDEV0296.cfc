component extends="org.lucee.cfml.test.LuceeTestCase"{
	// Test suite for LDEV-296: initmethod component attribute (ACF 2025 compatibility)
	// Key behaviors:
	// 1. initmethod takes complete precedence over init()
	// 2. No fallback - errors if method doesn't exist
	// 3. Not inherited - each component independent
	// 4. Return value honored - returns what initmethod returns
	// 5. Only new operator calls it (createObject/createComponent don't)
	// 6. Must be literal string (compile-time check)
	// 7. Empty string errors at runtime

	function run( testResults , testBox ) {
		describe( "initmethod attribute for components", function() {

			describe( "Basic Behavior", function() {

				it( 'Simple component with initmethod', function( currentSpec ) {
					uri = createURI( "LDEV0296/test-simple.cfm" );
					local.result = _InternalRequest( template=uri );
					assertEquals( "setup called", result.filecontent.trim() );
				});

				it( 'No initmethod uses init (baseline)', function( currentSpec ) {
					uri = createURI( "LDEV0296/test-baseline.cfm" );
					local.result = _InternalRequest( template=uri );
					assertEquals( "init called", result.filecontent.trim() );
				});

				it( 'Component with non-existent initmethod throws error', function( currentSpec ) {
					uri = createURI( "LDEV0296/test-missing-method.cfm" );
					local.result = _InternalRequest( template=uri );
					assertEquals( "PASS", result.filecontent.trim() );
				});

				it( 'Empty string initmethod throws error', function( currentSpec ) {
					uri = createURI( "LDEV0296/test-empty-initmethod.cfm" );
					local.result = _InternalRequest( template=uri );
					assertEquals( "PASS", result.filecontent.trim() );
				});
			});

			describe( "Inheritance", function() {

				it( 'Basic inheritance with same initmethod (original test)', function( currentSpec ) {
					uri = createURI( "LDEV0296/index.cfm" );
					local.result = _InternalRequest( template=uri );
					assertEquals( "40", result.filecontent.trim() );
				});

				it( 'Child overrides parent initmethod', function( currentSpec ) {
					uri = createURI( "LDEV0296/test-inheritance-override.cfm" );
					local.result = _InternalRequest( template=uri );
					assertEquals( "NO,YES", result.filecontent.trim() );
				});

				it( 'Parent has initmethod, child does not', function( currentSpec ) {
					uri = createURI( "LDEV0296/test-inheritance-mixed.cfm" );
					local.result = _InternalRequest( template=uri );
					assertEquals( "NO,YES", result.filecontent.trim() );
				});
			});

			describe( "createObject behavior", function() {

				it( 'createObject does not call initmethod', function( currentSpec ) {
					uri = createURI( "LDEV0296/test-createobject.cfm" );
					local.result = _InternalRequest( template=uri );
					assertEquals( "PASS", result.filecontent.trim() );
				});
			});

			describe( "Arguments", function() {

				it( 'Arguments passed to initmethod', function( currentSpec ) {
					uri = createURI( "LDEV0296/test-arguments.cfm" );
					local.result = _InternalRequest( template=uri );
					assertEquals( "Zac,35|Zac,35", result.filecontent.trim() );
				});
			});

			describe( "Return Values", function() {

				it( 'Return values from initmethod', function( currentSpec ) {
					uri = createURI( "LDEV0296/test-return-values.cfm" );
					local.result = _InternalRequest( template=uri );
					assertEquals( "test|test|object", result.filecontent.trim() );
				});
			});
		});
	}

	private string function createURI( string calledName ){
		var baseURI="/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ),"\/")#/";
		return baseURI&""&calledName;
	}
}