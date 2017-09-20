component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1503", function() {
			it( title='Checking JSON string, without quoted in key', body=function( currentSpec ) {
				var JSON = '{foo:"bar"}';
				assertEquals(false, isJSON( JSON ));
			});

			it( title='Checking JSON string, without quoted in value', body=function( currentSpec ) {
				var JSON = '{"foo":bar}';
				assertEquals(false, isJSON( JSON ));
			});

			it( title='Checking JSON string, without quoted in key & value', body=function( currentSpec ) {
				var JSON = '{foo : bar}';
				assertEquals(false, isJSON( JSON ));
			});

			it( title='Checking JSON string, with quoted in key & value', body=function( currentSpec ) {
				var JSON = '{"foo" : "bar"}';
				assertEquals(true, isJSON( JSON ));
			});
		});
	}
}