component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test Case for LDEV-884", function() {
			it( title='Checking DateDiff with a datepart of ww', body=function( currentSpec ) {
				ww = dateDiff("ww","2018-01-09","2018-01-16");
				expect(ww).toBe('1');
			});
			it( title='Checking DateDiff with a datepart of wd', body=function( currentSpec ) {
				w = dateDiff("wd","2018-01-09","2018-01-16");
				expect(w).toBe('5');
			});

			it( title='Checking DateDiff with a datepart of w', body=function( currentSpec ) {
				w = dateDiff("w","2018-01-09","2018-01-16");
				expect(w).toBe('1'); // in Lucee w is equal to ww, in ACF it is equal to wd
			});


			it( title='Checking DateDiff with a datepart of wd 2', body=function( currentSpec ) {
				w = dateDiff("wd","2018-01-09","2019-01-16");
				expect(w).toBe('266');
			});

		});
	}
}
