component extends="org.lucee.cfml.test.LuceeTestCase" labels="struct" {

	function beforeAll() {
		// quoted keys keep their case
		variables.textStruct = {
			"charlie": 3,
			"alpha": 1,
			"bravo": 2
		};
		variables.mixedCaseStruct = {
			"Zulu": 3,
			"alpha": 1,
			"MIKE": 2
		};
		variables.numericStruct = {
			"10": "ten",
			"2": "two",
			"1": "one"
		};
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-6302: toSorted() / structToSorted() with only a sort type defaults to ascending", body=function() {

			it( title="member toSorted('text') sorts ascending", body=function( currentSpec ) {
				expect( structKeyList( duplicate( variables.textStruct ).toSorted( "text" ) ) ).toBeWithCase( "alpha,bravo,charlie" );
			} );

			it( title="member toSorted('textNoCase') sorts ascending", body=function( currentSpec ) {
				expect( structKeyList( duplicate( variables.mixedCaseStruct ).toSorted( "textNoCase" ) ) ).toBeWithCase( "alpha,MIKE,Zulu" );
			} );

			it( title="member toSorted('numeric') sorts ascending", body=function( currentSpec ) {
				expect( structKeyList( duplicate( variables.numericStruct ).toSorted( "numeric" ) ) ).toBe( "1,2,10" );
			} );

			it( title="structToSorted( struct, 'textNoCase' ) sorts ascending", body=function( currentSpec ) {
				expect( structKeyList( structToSorted( duplicate( variables.mixedCaseStruct ), "textNoCase" ) ) ).toBeWithCase( "alpha,MIKE,Zulu" );
			} );

		} );
	}

}
