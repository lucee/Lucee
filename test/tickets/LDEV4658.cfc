component extends = "org.lucee.cfml.test.LuceeTestCase" labels="datetime" {

	function run( testResults, testBox ) {
		describe( "Testcase for LDEV-4725", function() {
			it( title="checking unicode compat", body=function( currentSpec ) {
				var d = CreateDateTime(2000,1,2,3,4,5,0,"UTC");
				var full = d.dateTimeFormat( "full","UTC" );
				var parsed = parseDateTime( full );
				expect ( dateCompare( d, parsed ) ).toBe( 0 );
				expect ( dateCompare( d, full ) ).toBe( 0 );
			});

			it( title="checking unicode compat - prejava 19, with space with parseDateTime", body=function( currentSpec ) {
				var d = CreateDateTime(2000,1,2,3,4,5,0,"UTC");
				var src = d.dateTimeFormat( "full","UTC" );
				var src = replace(src,chr(8239)&"AM",chr(32)&"AM");
				var parsed = parseDateTime(date: src, timezone:"UTC" );
				expect ( dateCompare( d, parsed ) ).toBe( 0 );
			});

			it( title="checking unicode compat - postjava 19, with Narrow No-Break Space with parseDateTime", body=function( currentSpec ) {
				var d = CreateDateTime(2000,1,2,3,4,5,0,"UTC");
				var src = d.dateTimeFormat( "full","UTC" );
				var src = replace(src,chr(32)&"AM",chr(8239)&"AM");
				var parsed = parseDateTime(date: src, timezone:"UTC" );
				expect ( dateCompare( d, parsed ) ).toBe( 0 );
			});


			it( title="checking unicode compat - prejava 19, with space with lsparseDateTime", body=function( currentSpec ) {
				var d = CreateDateTime(2000,1,2,3,4,5,0,"UTC");
				var src = d.lsdateTimeFormat( "full","en_us","UTC" );
				var src = replace(src,chr(8239)&"AM",chr(32)&"AM");
				debug(src);
				var parsed = lsparseDateTime(date: src,locale:"en_US", timezone:"UTC" );
				expect ( dateCompare( d, parsed ) ).toBe( 0 );
			});

			it( title="checking unicode compat - postjava 19, with Narrow No-Break Space with lsparseDateTime", body=function( currentSpec ) {
				var d = CreateDateTime(2000,1,2,3,4,5,0,"UTC");
				var src = d.lsdateTimeFormat( "full","en_us","UTC" );
				var src = replace(src,chr(32)&"AM",chr(8239)&"AM");
				debug(src);
				var parsed = lsparseDateTime(date: src,locale:"en_US", timezone:"UTC" );
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
