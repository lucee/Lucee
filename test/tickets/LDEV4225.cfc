component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV4225", function() {

			it( title='json parser error should provide context, src longer than 1024 characters', body=function( currentSpec ) {
				var str = '{ content: "#repeatString('a ',505)#" }7D';
				try {
					deserializeJson( str );
				} catch (e) {
					systemOutput("", true);
					systemOutput(e.message, true);
					systemOutput(e.detail, true);
					expect(e.message).toInclude("position");
				}
			});

			it( title='json parser error should provide context, src sorter than 1024 characters', body=function( currentSpec ) {
				var str = '{ content: "#repeatString('a ',5)#" }7D';
				try {
					deserializeJson( str );
				} catch (e) {
					systemOutput("", true);
					systemOutput(e.message, true);
					systemOutput(e.detail, true);
					expect(e.message).toInclude("position");
				}

			});

		});
	}

}