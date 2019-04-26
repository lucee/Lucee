component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for CreateODBCTime()", body=function() {
			it(title="checking CreateODBCTime() function", body = function( currentSpec ) {
				fixDate=CreateDateTime(2001, 11, 1, 4, 10, 4);
				assertEquals("{t '04:10:04'}x","#CreateODBCTime(fixDate)#x");
				assertEquals("#CreateODBCTime(1)#x","{t '00:00:00'}x");
			});
		});
	}
}
