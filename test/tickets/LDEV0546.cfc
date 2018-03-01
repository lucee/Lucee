component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-546", function() {
			it( title='datediff for ms', body=function( currentSpec ) {
				var d1=now();
				var d2=dateAdd("s",1,d1);
				assertEquals(1000, dateDiff('l',d1,d2));
			});
		});
	}
}