component extends="org.lucee.cfml.test.LuceeTestCase" labels="syntax" {

	function run( testResults, testBox ) {
		describe( title="LDEV-34 comment between a loop label and the loop", body=function() {

			it( title="label directly followed by a for loop", body=function( currentSpec ) {
				expect( new LDEV34.Labelled().forNoComment() ).toBe( 10 );
			});

			it( title="single line comment between the label and a for loop", body=function( currentSpec ) {
				expect( new LDEV34.Labelled().forSingleLineComment() ).toBe( 10 );
			});

			it( title="multi line comment between the label and a for loop", body=function( currentSpec ) {
				expect( new LDEV34.Labelled().forMultiLineComment() ).toBe( 10 );
			});

			it( title="single line comment between the label and a while loop", body=function( currentSpec ) {
				expect( new LDEV34.Labelled().whileSingleLineComment() ).toBe( 10 );
			});

			it( title="single line comment between the label and a do while loop", body=function( currentSpec ) {
				expect( new LDEV34.Labelled().doWhileSingleLineComment() ).toBe( 10 );
			});

		});
	}

}
