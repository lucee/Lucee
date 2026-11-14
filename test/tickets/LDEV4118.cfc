component extends="org.lucee.cfml.test.LuceeTestCase" labels="struct" {

	function run( testResults, testBox ) {
		describe( "Testcase for LDEV-4118", function() {

			beforeEach( function() {
				variables.animals = { cat: { activities: { sleep: "sleeping", eat: "eating" } } };
			});

			it( title="structGet() with a path to an existing key works", body=function( currentSpec ) {
				expect( structGet( "animals.cat.activities.sleep" ) ).toBe( "sleeping" );
			});

			it( title="structGet() with a numeric key that does not exist returns an empty struct, like for any other missing key", body=function( currentSpec ) {
				var missing = structGet( "animals.cat.activities.beach" );
				expect( isStruct( missing ) ).toBeTrue();
				var result = structGet( "animals.cat.activities.1" );
				expect( isStruct( result ) ).toBeTrue();
				expect( structIsEmpty( result ) ).toBeTrue();
			});

			it( title="structGet() with a numeric key that exists returns its value", body=function( currentSpec ) {
				variables.animals.cat.activities[ "9" ] = "lives";
				expect( structGet( "animals.cat.activities.9" ) ).toBe( "lives" );
				expect( variables.animals.cat.activities[ "9" ] ).toBe( "lives" );
			});

			it( title="structGet() with a numeric key does not change the existing value", body=function( currentSpec ) {
				variables.animals.cat.activities[ "10" ] = "ten";
				structGet( "animals.cat.activities.10" );
				expect( variables.animals.cat.activities[ "10" ] ).toBe( "ten" );
			});

			it( title="isDefined() finds a key that starts with a digit", body=function( currentSpec ) {
				variables.animals.cat.activities[ "9" ] = "lives";
				expect( isDefined( "animals.cat.activities.9" ) ).toBeTrue();
				expect( isDefined( "animals.cat.activities.8" ) ).toBeFalse();
			});
		});
	}
}
