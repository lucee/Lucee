component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for CreateDateTime()", body=function() {
			it(title="checking CreateDateTime() function", body = function( currentSpec ) {
				assertEquals("{ts '2000-12-11 00:00:00'}x","#CreateDateTime(2000, 12, 11)#x");
				assertEquals("{ts '2000-12-01 00:00:00'}x","#CreateDateTime(2000, 12)#x");
				assertEquals("{ts '2000-01-01 00:00:00'}x","#CreateDateTime(2000)#x");
				assertEquals("{ts '2000-01-23 00:05:00'}x","#CreateDateTime(year:2000,day:23,minute:5)#x");

				assertEquals("{ts '2000-12-01 12:11:10'}x","#CreateDateTime(2000, 12, 1,12,11,10)#x");
				assertEquals("#CreateODBCDateTime(1)#x","{ts '1899-12-31 00:00:00'}x" );
				assertEquals("#CreateODBCDateTime(1.1)#x","{ts '1899-12-31 02:24:00'}x");

				assertEquals("#CreateODBCDateTime(1.11)#x","{ts '1899-12-31 02:38:24'}x");
				assertEquals("#CreateODBCDateTime(1.111)#x","{ts '1899-12-31 02:39:50'}x");
				assertEquals("#CreateODBCDateTime(1.1111)#x","{ts '1899-12-31 02:39:59'}x");
				assertEquals("#CreateODBCDateTime(1.11111)#x","{ts '1899-12-31 02:39:59'}x");
    
				tz = createObject('java','java.util.TimeZone').getTimeZone("America/Mexico_City");
				c = createObject('java','java.util.Calendar').getInstance();
				c.setTimeZone(tz);
				dt=CreateDateTime(2000,1,1,1,1,1);
				c.setTime(dt);

				assertEquals("#dt#x","#c#x");
				assertEquals("#dt#x","#c#x");
				assertEquals("#dt&""#x","#c#x");
				assertEquals("#true#","#c EQ dt#");
			});
		});
	}
}