component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1553", function() {
			it( title='checking createTimeSpan() as boolean', body=function( currentSpec ) {
				var ts = createTimeSpan(0, 0, 0, 1);
				assertEquals(1, ts?1:0);
			});

			it( title='checking createTimeSpan() as number', body=function( currentSpec ) {
				var ts = createTimeSpan(1, 0, 0, 0);
				assertEquals(2, ts+1);
			});

			it( title='checking createTimeSpan() as datetime', body=function( currentSpec ) {
				var ts = createTimeSpan(0, 0, 0, 0);
				assertEquals(0, dateAdd("s",0,ts)+0);
			});
		});
	}
}