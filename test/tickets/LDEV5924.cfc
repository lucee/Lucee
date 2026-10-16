component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "LDEV-5924 PageContext pool size counter", function() {

			it( "reports an idle pool count within the configured maximum", function() {
				var pool = getSystemMetrics().pageContextPool;
				expect( pool.idle ).toBeNumeric();
				expect( pool.idle ).toBeGTE( 0 );
				expect( pool.idle ).toBeLTE( pool.max );
			} );

			it( "keeps the idle count consistent after concurrent threads", function() {
				var names = [];
				loop from=1 to=25 index="local.i" {
					var name = "ldev5924_#i#_#createUniqueId()#";
					arrayAppend( names, name );
					thread name="#name#" {
						sleep( 10 );
					}
				}
				thread action="join" name="#arrayToList( names )#";
				var pool = getSystemMetrics().pageContextPool;
				expect( pool.idle ).toBeGTE( 0 );
				expect( pool.idle ).toBeLTE( pool.max );
			} );

		} );
	}

}
