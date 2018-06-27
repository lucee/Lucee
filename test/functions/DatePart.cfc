component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for DatePart", function() {
			it(title = "Checking with DatePartMember", body = function( currentSpec ) {
				d1=CreateDateTime(2001, 12, 1, 4, 10, 1);
				assertEquals("12", "#d1.part("m")#");
			});
			
			it(title = "Checking with DatePart", body = function( currentSpec ) {
				d1=CreateDateTime(2001, 12, 1, 4, 10, 1);
				assertEquals("12", "#datePart("m",d1)#");

				assertEquals("#DatePart("yyyy", 1)#", "1899" );
				    
				assertEquals("#DatePart("w", d1)#", "7");
				assertEquals("#DatePart("ww", d1)#", "48");
				assertEquals("#DatePart("q", d1)#", "4");
				assertEquals("#DatePart("m", d1)#", "12");
				assertEquals("#DatePart("y", d1)#", "335");
				assertEquals("#DatePart("d", d1)#", "1");
				assertEquals("#DatePart("h", d1)#", "4");
				assertEquals("#DatePart("n", d1)#", "10");
				assertEquals("#DatePart("s", d1)#", "1");
				assertEquals("#DatePart("l", d1)#", "0");
			});
		});	
	}
}