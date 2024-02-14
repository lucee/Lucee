component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV4295", function() {

			it( title='when a datasource config struct is empty, throw a meaningful exception', body=function( currentSpec ) {
				try {
					var ds = {};
					application action="update" datasource="#ds#";
				} catch (e) {
					expect(e.message).toInclude("was empty");
				}
			});

		});
	}

}