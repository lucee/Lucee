component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for CreateDate()", body=function() {
			it(title="checking CreateDate() function", body = function( currentSpec ) {
				assertEquals("{ts '2000-12-01 00:00:00'}x","#CreateDate(2000, 12)#x");
				assertEquals("{ts '2000-01-01 00:00:00'}x","#CreateDate(2000)#x");
				assertEquals("{ts '2000-01-03 00:00:00'}x","#CreateDate(year:2000,day:03)#x");

				assertEquals("{ts '2000-12-01 00:00:00'}x","#CreateDate(2000, 12, 1)#x");
				assertEquals("#CreateODBCDate(1)#x","{d '1899-12-31'}x");
					
				d = CreateDate(2007,11,30);
				d1 = d - 0;
				assertEquals("39416","#d1#");

				d = CreateDate(2007,5,1);
				d1 = d - 0;
				assertEquals("39203","#round(d1)#");
			});
		});
	}
}
