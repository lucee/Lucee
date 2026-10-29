component extends="org.lucee.cfml.test.LuceeTestCase" labels="json" {

	function run( testResults, testBox ) {
		describe( "LDEV-1503 isJson(), argument lenient", function() {

			describe( "lenient=false, only strict JSON (RFC 8259) is valid", function() {

				it( title="valid documents", body=function( currentSpec ) {
					var valid = [
						'{}', '[]', '{"foo":"bar"}', '{"foo" : "bar"}', '["a", 1, -2.5, 1e10, 1E+2, 1.5e-3, true, false, null]',
						'{"a":{"b":[1,2,{"c":null}]}}', '"a string"', '123', '-0', '0.5', 'true', 'false', 'null',
						' {"foo":"bar"} ', chr( 9 ) & '[1]' & chr( 10 ) & chr( 13 ),
						'"\" \\ \/ \b \f \n \r \t \u00e9 \uD83D\uDE00"',
						'{"":1}', '{"a":"##susi##"}', '"' & chr( 128515 ) & '"'
					];
					loop array=valid item="local.json" {
						expect( isJSON( json, "json", false ) ).toBeTrue( json );
						expect( isJSON( var=json, lenient=false ) ).toBeTrue( json );
					}
				});

				it( title="unquoted key", body=function( currentSpec ) {
					expect( isJSON( '{foo:"bar"}', "json", false ) ).toBeFalse();
					expect( isJSON( var='{foo:"bar"}', lenient=false ) ).toBeFalse();
				});

				it( title="unquoted value", body=function( currentSpec ) {
					expect( isJSON( '{"foo":bar}', "json", false ) ).toBeFalse();
					expect( isJSON( '{foo : bar}', "json", false ) ).toBeFalse();
					expect( isJSON( '[a,b,c]', "json", false ) ).toBeFalse();
				});

				it( title="single quotes", body=function( currentSpec ) {
					expect( isJSON( "{'foo':'bar'}", "json", false ) ).toBeFalse();
					expect( isJSON( "{""foo"":'bar'}", "json", false ) ).toBeFalse();
					expect( isJSON( "'bar'", "json", false ) ).toBeFalse();
				});

				it( title="trailing comma and missing values", body=function( currentSpec ) {
					expect( isJSON( '[1,2,]', "json", false ) ).toBeFalse();
					expect( isJSON( '{"a":1,}', "json", false ) ).toBeFalse();
					expect( isJSON( '[,1]', "json", false ) ).toBeFalse();
					expect( isJSON( '[1,,2]', "json", false ) ).toBeFalse();
					expect( isJSON( '{"a":}', "json", false ) ).toBeFalse();
					expect( isJSON( '{"a"}', "json", false ) ).toBeFalse();
				});

				it( title="comments", body=function( currentSpec ) {
					expect( isJSON( '{"a":1} // comment', "json", false ) ).toBeFalse();
					expect( isJSON( '/* c */ {"a":1}', "json", false ) ).toBeFalse();
				});

				it( title="invalid numbers", body=function( currentSpec ) {
					var invalid = [ '01', '+1', '.5', '1.', '0x1F', '1e', '1e+', '--1', '-', 'NaN', 'Infinity', '-Infinity', '1 2' ];
					loop array=invalid item="local.json" {
						expect( isJSON( json, "json", false ) ).toBeFalse( json );
					}
				});

				it( title="invalid strings", body=function( currentSpec ) {
					expect( isJSON( '"unclosed', "json", false ) ).toBeFalse();
					expect( isJSON( '"bad \x escape"', "json", false ) ).toBeFalse();
					expect( isJSON( '"bad \u12G4 escape"', "json", false ) ).toBeFalse();
					expect( isJSON( '"short \u12', "json", false ) ).toBeFalse();
					expect( isJSON( '"a' & chr( 10 ) & 'b"', "json", false ) ).toBeFalse( "raw line break in a string" );
					expect( isJSON( '"a' & chr( 9 ) & 'b"', "json", false ) ).toBeFalse( "raw tab in a string" );
				});

				it( title="CFML expressions are not evaluated", body=function( currentSpec ) {
					expect( isJSON( '{"a":1+1}', "json", false ) ).toBeFalse();
					expect( isJSON( '{"a":now()}', "json", false ) ).toBeFalse();
					expect( isJSON( '{"a":susi=1}', "json", false ) ).toBeFalse();
					expect( isJSON( '[1,2]##', "json", false ) ).toBeFalse();
				});

				it( title="broken structure", body=function( currentSpec ) {
					var invalid = [ '', ' ', '{', '}', '[', ']', '{"a":1', '[1,2', '{"a":1}}', '[1]]', '[1] x', '{"a":1 "b":2}', '[1 2]', '{"a" 1}', '{1:2}', '[}', '{]', '{"a":[1}', 'tru', 'nul', 'truee' ];
					loop array=invalid item="local.json" {
						expect( isJSON( json, "json", false ) ).toBeFalse( json );
					}
				});

				it( title="deeply nested input does not overflow the stack", body=function( currentSpec ) {
					var depth = 100000;
					var open = repeatString( "[", depth );
					var close = repeatString( "]", depth );
					expect( isJSON( open & close, "json", false ) ).toBeTrue();
					expect( isJSON( open & close & "]", "json", false ) ).toBeFalse();
					expect( isJSON( open, "json", false ) ).toBeFalse();
				});

				it( title="format json5 is not affected, it is lenient by definition", body=function( currentSpec ) {
					expect( isJSON( '{foo:"bar"}', "json5", false ) ).toBeTrue();
				});

				it( title="invalid format still throws", body=function( currentSpec ) {
					expect( function() {
						isJSON( '{}', "xml", false );
					}).toThrow();
				});
			});

			describe( "lenient=true, the lenient behaviour stays", function() {

				it( title="unquoted key is accepted", body=function( currentSpec ) {
					expect( isJSON( '{foo:"bar"}', "json5", true ) ).toBeTrue();
					expect( isJSON( var='{foo:"bar"}', lenient=true ) ).toBeTrue();
				});

				it( title="unquoted variable as value is not accepted", body=function( currentSpec ) {
					expect( isJSON( '{"foo":bar}', "json5", true ) ).toBeFalse();
					expect( isJSON( '{foo : bar}', "json5", true ) ).toBeFalse();
				});

				it( title="json5 comment is accepted with format json5 only", body=function( currentSpec ) {
					var str = '{"a":1} // comment';
					expect( isJSON( str, "json5", true ) ).toBeTrue();
					expect( isJSON( str, "json", true ) ).toBeFalse();
				});

				it( title="valid JSON is valid", body=function( currentSpec ) {
					expect( isJSON( '{"foo" : "bar"}', "json", true ) ).toBeTrue();
					expect( isJSON( '{"foo" : "bar"}', "json5", true ) ).toBeTrue();
					expect( isJSON( var='{"foo" : "bar"}', lenient=true ) ).toBeTrue();
				});
			});

			describe( "the default is lenient=false", function() {

				it( title="only strict JSON is valid by default", body=function( currentSpec ) {
					expect( isJSON( '{"foo":"bar"}' ) ).toBeTrue();
					expect( isJSON( '{foo:"bar"}' ) ).toBeFalse();
					expect( isJSON( "{'foo':'bar'}" ) ).toBeFalse();
					expect( isJSON( '[1,2,]' ) ).toBeFalse();
					expect( isJSON( '{"a":1} // comment' ) ).toBeFalse();
					expect( isJSON( '{foo:"bar"}', "json" ) ).toBeFalse();
				});

				it( title="format json5 and lenient=true still allow the lenient syntax", body=function( currentSpec ) {
					expect( isJSON( '{foo:"bar"}', "json5" ) ).toBeTrue();
					expect( isJSON( '{"foo":"bar"}', "json", true ) ).toBeTrue();
					expect( isJSON( var='{foo:"bar"}', lenient=true ) ).toBeTrue();
				});

				it( title="isValid json follows the default", body=function( currentSpec ) {
					expect( isValid( "json", '{"foo":"bar"}' ) ).toBeTrue();
					expect( isValid( "json", '{foo:"bar"}' ) ).toBeFalse();
				});
			});

			describe( "deserializeJSON stays lenient", function() {

				it( title="accepts the unquoted key", body=function( currentSpec ) {
					expect( deserializeJSON( '{foo:"bar"}' ).foo ).toBe( "bar" );
				});
			});
		});
	}
}
