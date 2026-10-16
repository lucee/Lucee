component extends="org.lucee.cfml.test.LuceeTestCase"{

	function run( testResults , testBox ) {
		describe( "test case for LDEV-935", function() {
			it( title = "cfcontinue with cfsilent", body=function( currentSpec ) {
				param name="count" default="0";
				for( i=1; i<=5; i++) {
					cfsilent() {
						count = count+1;
						continue;
			            if(false) break;
					}
				}
				expect(count).toBe(5);
			});
		});
	}

}