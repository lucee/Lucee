component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test Case for LDEV-884", function() {
			it( title='Checking DateDiff with a datepart of ww', body=function( currentSpec ) {
				ww = dateDiff("ww","2018-01-09","2018-01-16");
				expect(ww).toBe('1');
			});
			it( title='Checking DateDiff with a datepart of w', body=function( currentSpec ) {
				w = dateDiff("w","2018-01-09","2018-01-16");
				expect(w).toBe('7');
			});
		});
	}
}
