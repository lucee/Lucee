component extends="org.lucee.cfml.test.LuceeTestCase"{

	function run( testResults , testBox ) {
		describe( "Test case for LDEV-2772", function() {
			it( title = "enable/disable search result", body = function( currentSpec ) {
				var q=query(a:[1]);
				
				try {
					application action="update" searchResults=false;
					loop query="q" {
						assertEquals(true,isNull(a));
					}

					application action="update" searchQueries=true;
					loop query="q" {
						assertEquals(false,isNull(a));
					}
				}
				finally {
					application action="update" searchResults=true;
				}
			});
		});
	}
}