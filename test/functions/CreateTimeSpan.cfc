component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for CreateTimeSpan()", body=function() {
			it(title="checking CreateTimeSpan() function", body = function( currentSpec ) {
				assertEquals("1.0423726851851853:","#CreateTimeSpan(1, 1, 1, 1)#:");
				assertEquals("12:30:00","#timeFormat(CreateTimeSpan(0,0,30,0),"hh:mm:ss")#");
				assertEquals("30.12.1899","#dateFormat(CreateTimeSpan(0,0,30,0),"dd.mm.yyyy")#");
			});
		});
	}
}
