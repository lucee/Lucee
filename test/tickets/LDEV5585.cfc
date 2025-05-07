component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function run( testResults, testBox ){
		describe( "Test LDEV-5585", function(){

			beforeEach( function(){
				variables.startingTZ=getTimeZone();
				setTimeZone("UTC");
            });
			afterEach( function(){
                setTimeZone(variables.startingTZ?:"UTC");
            });
			it( "test EEE, dd MMM yyyy HH:mm:ss", function(){				
				var result=isDate(ParseDateTime("May, 09 2025 14:36:15"));
				expect( result ).toBeTrue();
			});

		} );
	}

}
