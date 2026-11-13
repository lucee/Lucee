component extends="org.lucee.cfml.test.LuceeTestCase" labels="dump" {

	function run( testResults, testBox ) {
		describe( "LDEV-2669 dump with format=text must not contain html markup", function() {

			it( title="a simple value is written as plain text", body=function() {
				savecontent variable="local.result" {
					dump( var="abc", format="text" );
				}
				expect( result ).toInclude( "abc" );
				expect( result ).notToInclude( "<" );
				expect( result ).notToInclude( ">" );
			});

			it( title="a struct with an array is written as plain text", body=function() {
				savecontent variable="local.result" {
					dump( var={ susi: 1, sorglos: [ "a", "b" ] }, format="text" );
				}
				expect( result ).toInclude( "SUSI" );
				expect( result ).toInclude( "SORGLOS" );
				expect( result ).notToInclude( "<" );
				expect( result ).notToInclude( ">" );
			});

			it( title="the default format in the browser still contains html", body=function() {
				savecontent variable="local.result" {
					dump( var="abc" );
				}
				expect( result ).toInclude( "-lucee-dump" );
			});

		});
	}

}
