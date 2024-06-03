component extends = "org.lucee.cfml.test.LuceeTestCase" labels="datetime" skip="true" {

	function run( testResults, testBox ) {
		describe( "Testcase for LDEV-4725", function() {
			it( title="checking unicode compat", body=function( currentSpec ) {
				var d = CreateDateTime(2000,1,2,3,4,5,0,"CET");
				var full = d.dateTimeFormat( "full" );
				var parsed = parseDateTime( full );
				debugDateTime( full );
				expect ( dateCompare( d, parsed ) ).toBe( 0 );
				expect ( dateCompare( d, full ) ).toBe( 0 );
			});

			it( title="checking unicode compat - prejava 19, with space", body=function( currentSpec ) {
				var d = CreateDateTime(2000,1,2,3,4,5,0,"CET");
				var prejava19 = "Sunday, January 2, 2000 2:04:05 AM UTC"; // space before AM
				var parsed = parseDateTime( prejava19, "full" );
				debugDateTime( parsed );
				expect ( dateCompare( d, parsed ) ).toBe( 0 );
			});

			it( title="checking unicode compat - postjava 19, with Narrow No-Break Space", body=function( currentSpec ) {
				var d = CreateDateTime(2000,1,2,3,4,5,0,"CET");
				var postjava19 = "Sunday, January 2, 2000 2:04:05#chr(8239)#AM UTC"; // Narrow No-Break Space before AM
				var parsed = parseDateTime( postjava19, "full" );
				debugDateTime( parsed );
				expect ( dateCompare( d, parsed ) ).toBe( 0 );
			});
		});
	}
	
	private function debugDateTime( d ){
		systemOutput( "", true );
		systemOutput( d, true );
		for (var c=1; c < d.len(); c++){
			systemOutput( d[c] & " [" & asc(d[c]) & "]", true );
		}
	}
}
