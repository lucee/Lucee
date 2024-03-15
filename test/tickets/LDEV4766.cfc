component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {

		describe( "Test case for LDEV-4766", function() {
			it( title="Test case for lsParseCurrency is not thread safe", body=function( currentSpec ) {
				var a = [];
				arraySet( a, 1, 1000, "" );

				arrayEach( a, function( el, idx ){
					lsParseCurrency( arguments.idx & "." & RandRange( 1,100 ) );
				}, true );
			});
		});

	}

}
