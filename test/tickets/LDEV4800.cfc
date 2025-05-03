component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4800", function() {
			it( title='checking deserializeJSON() with string of struct' , body=function( currentSpec ) {
				var JsonTestOne = '[
					"id": 1,
					"john": 2
				]';
				expect( isJson(JsonTestOne) ).toBeFalse();
				expect( function() {
					deserializeJSON(JsonTestOne);
				}).toThrow();
			});
			it( title='checking deserializeJSON() with string of Array' , body=function( currentSpec ) {
				var JsonTestTwo = '[
					"id : 1",
					"john : 2"
				]';
				expect( isJson(JsonTestTwo) ).toBeTrue();
				expect( deserializeJSON(JsonTestTwo) ).toBeTypeOf( 'Array' );
			});
		});
	}

}