component extends="org.lucee.cfml.test.LuceeTestCase" labels="repeatstring"{

	function run( testResults , testBox ) {
		describe( title = "Testcase for RepeatString() function", body = function() {
			it( title = "checking RepeatString() function", body = function( currentSpec ) {
				str="I love Lucee ";
				expect(repeatString(str,2)).tobe("I love Lucee I love Lucee ");
				expect(repeatString(str,2).len()).tobe(26);
				expect(str.repeatString(3)).tobe("I love Lucee I love Lucee I love Lucee ");
				expect(str.repeatString(3).len()).tobe(39);
			});
		});
	}
}