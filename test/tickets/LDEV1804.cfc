component extends="org.lucee.cfml.test.LuceeTestCase" labels="json" {

	function beforeAll() {
		variables.script = new LDEV1804.Script();
		variables.tag = new LDEV1804.Tag();
	}

	function run( testResults, testBox ) {
		describe( "LDEV-1804 type=json for arguments and return types", function() {

			it( "script: a json object string is accepted and passed on unchanged", function() {
				var json = '{"a":1,"b":[1,2,3],"c":{"d":"e"}}';
				var res = variables.script.echo( json );
				expect( isSimpleValue( res ) ).toBeTrue();
				expect( res ).toBe( json );
			});

			it( "script: a json array string is accepted", function() {
				expect( variables.script.echo( '[1,2,3]' ) ).toBe( '[1,2,3]' );
			});

			it( "script: json scalars are accepted like isJSON does", function() {
				expect( variables.script.echo( '"abc"' ) ).toBe( '"abc"' );
				expect( variables.script.echo( 'true' ) ).toBe( 'true' );
				expect( variables.script.echo( 'null' ) ).toBe( 'null' );
				expect( variables.script.echo( '12.5' ) ).toBe( '12.5' );
			});

			it( "script: the string is not deserialized and keeps its formatting", function() {
				var json = '{ "a" : 1 }';
				expect( variables.script.echo( json ) ).toBe( json );
			});

			it( "script: the type name is not case sensitive", function() {
				expect( variables.script.typeUpper( '{"a":1}' ) ).toBe( '{"a":1}' );
				expect( function() { variables.script.typeUpper( 'nope' ); } ).toThrow();
			});

			it( "script: a string that is not json throws a type error", function() {
				var msg = "";
				try {
					variables.script.echo( "this is not json" );
				}
				catch ( any e ) {
					msg = e.message;
				}
				expect( msg ).notToBe( "" );
				expect( msg ).toInclude( "data" );
				expect( msg ).toInclude( "json" );
			});

			it( "script: broken json throws", function() {
				expect( function() { variables.script.echo( '{"a":' ); } ).toThrow();
				expect( function() { variables.script.echo( '[1,2' ); } ).toThrow();
			});

			it( "script: an empty string throws", function() {
				expect( function() { variables.script.echo( "" ); } ).toThrow();
				expect( function() { variables.script.echo( "   " ); } ).toThrow();
			});

			it( "script: a struct or an array is not accepted, no automatic serializing", function() {
				expect( function() { variables.script.echo( { a: 1 } ); } ).toThrow();
				expect( function() { variables.script.echo( [ 1, 2 ] ); } ).toThrow();
			});

			it( "script: json array types validate every item", function() {
				expect( variables.script.many( [ '{"a":1}', '[1]' ] ) ).toBe( 2 );
				expect( function() { variables.script.many( [ '{"a":1}', 'x' ] ); } ).toThrow();
			});

			it( "script: a default value is used and validated", function() {
				expect( variables.script.optional() ).toBe( "[]" );
				expect( function() { variables.script.badDefault(); } ).toThrow();
			});

			it( "script: json as function return type accepts json", function() {
				expect( variables.script.returnValue( '{"a":1}' ) ).toBe( '{"a":1}' );
			});

			it( "script: json as function return type throws for other values", function() {
				expect( function() { variables.script.returnValue( "no json" ); } ).toThrow();
				expect( function() { variables.script.returnValue( { a: 1 } ); } ).toThrow();
			});

			it( "tag: cfargument type=json accepts json and passes it on unchanged", function() {
				expect( variables.tag.echo( '{"a":[1,2]}' ) ).toBe( '{"a":[1,2]}' );
				expect( variables.tag.optional() ).toBe( "{}" );
			});

			it( "tag: cfargument type=json throws for a value that is not json", function() {
				expect( function() { variables.tag.echo( "abc" ); } ).toThrow();
				expect( function() { variables.tag.echo( { a: 1 } ); } ).toThrow();
			});

			it( "tag: cffunction returntype=json is validated", function() {
				expect( variables.tag.badReturn( '[1]' ) ).toBe( '[1]' );
				expect( function() { variables.tag.badReturn( "abc" ); } ).toThrow();
			});

			it( "closure: typed json arguments are validated as well", function() {
				var fn = function( required json data ) { return data; };
				expect( fn( '{"x":1}' ) ).toBe( '{"x":1}' );
				expect( function() { fn( "x" ); } ).toThrow();
			});

			it( "isValid knows the type json", function() {
				expect( isValid( "json", '{"a":1}' ) ).toBeTrue();
				expect( isValid( "json", "abc" ) ).toBeFalse();
				expect( isValid( "json", { a: 1 } ) ).toBeFalse();
			});

			it( "a component named json still matches type=json", function() {
				var c = new LDEV1804.json();
				var fn = function( required json data ) { return data; };
				expect( isObject( fn( c ) ) ).toBeTrue();
			});

		});
	}

}
