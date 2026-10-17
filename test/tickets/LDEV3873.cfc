component extends = "org.lucee.cfml.test.LuceeTestCase" labels="array" {
	function run( testResults, textbox ) {
		describe("testcase for LDEV-3873", function() {
			it(title="Checking arrayEvery() method with empty array", body=function( currentSpec ) {
				var testArray  = [];
				var result = arrayEvery( testArray , function( value ) { true; } );
				expect( result ).toBeTrue();
			});
			it(title="Checking array.every() member function with empty array", body=function( currentSpec ) {
				expect( [].every( function( value ) { return false; } ) ).toBeTrue();
			});
			it(title="every() with an empty array behaves like with an empty struct, query or list", body=function( currentSpec ) {
				var fn = function() { return false; };
				expect( arrayEvery( [], fn ) ).toBe( structEvery( {}, fn ) );
				expect( arrayEvery( [], fn ) ).toBe( queryNew( "a" ).every( fn ) );
				expect( arrayEvery( [], fn ) ).toBe( listEvery( "", fn ) );
			});
			it(title="every() with a not empty array is unchanged", body=function( currentSpec ) {
				expect( [ 1, 2 ].every( function( value ) { return value > 0; } ) ).toBeTrue();
				expect( [ 1, 2 ].every( function( value ) { return value > 1; } ) ).toBeFalse();
			});
		});
	}
}
