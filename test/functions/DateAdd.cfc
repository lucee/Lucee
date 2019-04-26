component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for DateAdd()", body=function() {
			it(title="checking DateAdd() function with testDateAddMember", body = function( currentSpec ) {
				fixDate=CreateDateTime(2001, 11, 1, 4, 10, 4);
				assertEquals("{ts '2002-11-01 04:10:04'}","#fixDate.Add("yyyy", 1)#");
				assertEquals("{ts '2011-11-01 04:10:04'}","#fixDate.Add("yyyy", 10)#");
				assertEquals("{ts '123458790-11-01 04:10:04'}","#fixDate.Add("yyyy", 123456789)#");
			});
			it(title="checking DateAdd() function with testDateAdd", body = function( currentSpec ) {
				setTimeZone('Europe/Berlin');
				fixDate=CreateDateTime(2001, 11, 1, 4, 10, 4);
				assertEquals("{ts '2002-11-01 04:10:04'}","#DateAdd("yyyy", 1, fixDate)#");
				assertEquals("{ts '2011-11-01 04:10:04'}","#DateAdd("yyyy", 10, fixDate)#");
				assertEquals("{ts '123458790-11-01 04:10:04'}","#DateAdd("yyyy", 123456789, fixDate)#");

				assertEquals("{ts '2002-02-01 04:10:04'}","#DateAdd("q", 1, fixDate)#");
				assertEquals("{ts '2004-05-01 04:10:04'}","#DateAdd("q", 10, fixDate)#");
				assertEquals("{ts '30866199-02-01 04:10:04'}","#DateAdd("q", 123456789, fixDate)#");

				assertEquals("{ts '2001-12-01 04:10:04'}","#DateAdd("m", 1, fixDate)#");
				assertEquals("{ts '2002-09-01 04:10:04'}","#DateAdd("m", 10, fixDate)#");
				assertEquals("{ts '10290067-08-01 04:10:04'}","#DateAdd("m", 123456789, fixDate)#");
				 
				assertEquals("{ts '2001-11-02 04:10:04'}","#DateAdd("y", 1, fixDate)#");
				assertEquals("{ts '2001-11-11 04:10:04'}","#DateAdd("y", 10, fixDate)#");
				assertEquals("{ts '340015-01-16 04:10:04'}","#DateAdd("y", 123456789, fixDate)#");

				assertEquals("{ts '2001-11-02 04:10:04'}","#DateAdd("d", 1, fixDate)#");
				assertEquals("{ts '2001-11-11 04:10:04'}","#DateAdd("d", 10, fixDate)#");
				assertEquals("{ts '3077922-01-19 04:10:04'}","#DateAdd("d", 1123456789, fixDate)#");

				assertEquals("{ts '2001-11-02 04:10:04'}","#DateAdd("w", 1, fixDate)#");
				assertEquals("{ts '2001-11-15 04:10:04'}","#DateAdd("w", 10, fixDate)#");
				assertEquals("{ts '4308290-02-19 04:10:04'}","#DateAdd("w", 1123456789, fixDate)#");

				assertEquals("{ts '2001-11-08 04:10:04'}","#DateAdd("ww", 1, fixDate)#");
				assertEquals("{ts '2002-01-10 04:10:04'}","#DateAdd("ww", 10, fixDate)#");
				assertEquals("{ts '2217-02-20 04:10:04'}","#DateAdd("ww", 11234, fixDate)#");

				assertEquals("{ts '2001-11-01 05:10:04'}","#DateAdd("h", 1, fixDate)#");
				assertEquals("{ts '2001-11-01 14:10:04'}","#DateAdd("h", 10, fixDate)#");
				assertEquals("{ts '130165-03-05 17:10:04'}","#DateAdd("h", 1123456789, fixDate)#");

				assertEquals("{ts '2001-11-01 04:11:04'}","#DateAdd("n", 1, fixDate)#");
				assertEquals("{ts '2001-11-01 04:20:04'}","#DateAdd("n", 10, fixDate)#");
				assertEquals("{ts '4137-11-21 11:59:04'}","#DateAdd("n", 1123456789, fixDate)#");

				assertEquals("{ts '2001-11-01 04:10:05'}","#DateAdd("s", 1, fixDate)#");
				assertEquals("{ts '2001-11-01 04:10:14'}","#DateAdd("s", 10, fixDate)#");

				assertEquals("{ts '2034-03-14 18:14:17'}","#DateAdd("s", 1021385053, fixDate)#");
				try {
			        assertEquals("{ts '2001-11-01 04:10:05'}","#DateAdd("peter", 1, fixDate)#");
			        fail("must throw:DateAdd(""peter"", 1, fixDate)");
				} catch(any e){}

				assertEquals("{ts '1900-02-28 00:00:00'}","#DateAdd("m", 1, "{ts '1900-01-31 00:00:00'}")&""#");
				assertEquals("{ts '1901-01-31 00:00:00'}","#DateAdd("yyyy", 1, "{ts '1900-01-31 00:00:00'}")&""#");
				assertEquals("{ts '1900-02-28 00:00:00'}","#DateAdd("m", 1, 32)&""#");
				assertEquals("{ts '1901-01-31 00:00:00'}","#DateAdd("yyyy", 1, 32)&""#");
				assertEquals("{ts '1900-01-31 00:00:00'}","#DateAdd("m", 1, "{ts '1899-12-31 00:00:00'}")&""#");

				assertEquals("#DateAdd("yyyy", 2, 1).getTime()#","#parseDateTime("{ts '1901-12-31 00:00:00'}").getTime()#");
				assertEquals("#DateAdd("yyyy", 2, 1)#","#parseDateTime("{ts '1901-12-31 00:00:00'}")#" );
				assertEquals("#DateAdd("yyyy", 2, 1)#","{ts '1901-12-31 00:00:00'}");
					
				date=CreateDateTime(2008,10,28,0,0,0);
				assertEquals("+{ts '2008-10-28 00:00:00'}+","+#date#+");
				assertEquals("+39750+","+#date+1#+");
				assertEquals("+{ts '1899-12-30 00:00:00'}+","+#DateAdd('d',0,0)#+");
				assertEquals("+{ts '2008-10-29 00:00:00'}+","+#DateAdd('d',0,date+1)#+");

				date1=CreateDate(2009, 1, 1);
				date2=DateAdd('m', 1, date1);
				assertEquals("{ts '2009-01-01 00:00:00'}x","#date1#x");
				assertEquals("{ts '2009-02-01 00:00:00'}x","#date2#x");
				assertEquals("1x","#DateDiff('m', date1, date2)#x");
				assertEquals("31x","#DateDiff('d', date1, date2)#x");
				                 
				date1=CreateDate(2009, 2, 1);
				date2=DateAdd('m', 1, date1);
				assertEquals("{ts '2009-02-01 00:00:00'}x","#date1#x");
				assertEquals("{ts '2009-03-01 00:00:00'}x","#date2#x");
				assertEquals("1x","#DateDiff('m', date1, date2)#x");
				assertEquals("28x","#DateDiff('d', date1, date2)#x");
				                
				date1=CreateDate(2009, 3, 1);
				date2=DateAdd('m', 1, date1);
				assertEquals("{ts '2009-03-01 00:00:00'}x","#date1#x");
				assertEquals("{ts '2009-04-01 00:00:00'}x","#date2#x");
				assertEquals("1x","#DateDiff('m', date1, date2)#x");
				assertEquals("31x","#DateDiff('d', date1, date2)#x");
				                
				date1=CreateDate(2009, 4, 1);
				date2=DateAdd('m', 1, date1);
				assertEquals("{ts '2009-04-01 00:00:00'}x","#date1#x");
				assertEquals("{ts '2009-05-01 00:00:00'}x","#date2#x");
				assertEquals("1x","#DateDiff('m', date1, date2)#x");
				assertEquals("30x","#DateDiff('d', date1, date2)#x");
				                
				date1=CreateDate(2009, 5, 1);
				date2=DateAdd('m', 1, date1);
				assertEquals("{ts '2009-05-01 00:00:00'}x","#date1#x");
				assertEquals("{ts '2009-06-01 00:00:00'}x","#date2#x");
				assertEquals("1x","#DateDiff('m', date1, date2)#x");
				assertEquals("31x","#DateDiff('d', date1, date2)#x");
				                
				date1=CreateDate(2009, 6, 1);
				date2=DateAdd('m', 1, date1);
				assertEquals("{ts '2009-06-01 00:00:00'}x","#date1#x");
				assertEquals("{ts '2009-07-01 00:00:00'}x","#date2#x");
				assertEquals("1x","#DateDiff('m', date1, date2)#x");
				assertEquals("30x","#DateDiff('d', date1, date2)#x");
				                
				date1=CreateDate(2009, 7, 1);
				date2=DateAdd('m', 1, date1);
				assertEquals("{ts '2009-07-01 00:00:00'}x","#date1#x");
				assertEquals("{ts '2009-08-01 00:00:00'}x","#date2#x");
				assertEquals("1x","#DateDiff('m', date1, date2)#x");
				assertEquals("31x","#DateDiff('d', date1, date2)#x");
				                
				date1=CreateDate(2009, 8, 1);
				date2=DateAdd('m', 1, date1);
				assertEquals("{ts '2009-08-01 00:00:00'}x","#date1#x");
				assertEquals("{ts '2009-09-01 00:00:00'}x","#date2#x");
				assertEquals("1x","#DateDiff('m', date1, date2)#x");
				assertEquals("31x","#DateDiff('d', date1, date2)#x");
				               
				date1=CreateDate(2009, 9, 1);
				date2=DateAdd('m', 1, date1);
				assertEquals("{ts '2009-09-01 00:00:00'}x","#date1#x");
				assertEquals("{ts '2009-10-01 00:00:00'}x","#date2#x");
				assertEquals("1x","#DateDiff('m', date1, date2)#x");
				assertEquals("30x","#DateDiff('d', date1, date2)#x");
				                
				date1=CreateDate(2009, 10, 1);
				date2=DateAdd('m', 1, date1);
				assertEquals("{ts '2009-10-01 00:00:00'}x","#date1#x");
				assertEquals("{ts '2009-11-01 00:00:00'}x","#date2#x");
				assertEquals("1x","#DateDiff('m', date1, date2)#x");
				assertEquals("31x","#DateDiff('d', date1, date2)#x");
				                
				date1=CreateDate(2009, 11, 1);
				date2=DateAdd('m', 1, date1);
				assertEquals("{ts '2009-11-01 00:00:00'}x","#date1#x");
				assertEquals("{ts '2009-12-01 00:00:00'}x","#date2#x");
				assertEquals("1x","#DateDiff('m', date1, date2)#x");
				assertEquals("30x","#DateDiff('d', date1, date2)#x");
				                
				date1=CreateDate(2009, 12, 1);
				date2=DateAdd('m', 1, date1);
				assertEquals("{ts '2009-12-01 00:00:00'}x","#date1#x");
				assertEquals("{ts '2010-01-01 00:00:00'}x","#date2#x");
				assertEquals("1x","#DateDiff('m', date1, date2)#x");
				assertEquals("31x","#DateDiff('d', date1, date2)#x");

				assertEquals("{ts '1900-01-30 00:00:00'}x","#DateAdd("m", 1, 0)#x");
				assertEquals("{ts '1901-12-30 00:00:00'}","#DateAdd("yyyy", 2, 0)&""#");

				assertEquals("{ts '1975-11-01 00:00:00'}x","#DateAdd('m',0,"11/01/1975 00:00 AM")#x");
				assertEquals("{ts '1975-11-01 00:01:00'}x","#DateAdd('m',0,"11/01/1975 00:01 AM")#x");
				assertEquals("{ts '1975-11-01 11:59:00'}x","#DateAdd('m',0,"11/01/1975 11:59 AM")#x");
				assertEquals("{ts '1975-11-01 00:00:00'}x","#DateAdd('m',0,"11/01/1975 12:00 AM")#x");
				assertEquals("{ts '1975-11-01 00:01:00'}x","#DateAdd('m',0,"11/01/1975 12:01 AM")#x");

				assertEquals("{ts '1975-11-01 12:00:00'}x","#DateAdd('m',0,"11/01/1975 00:00 PM")#x");
				assertEquals("{ts '1975-11-01 12:01:00'}x","#DateAdd('m',0,"11/01/1975 00:01 PM")#x");
				assertEquals("{ts '1975-11-01 23:59:00'}x","#DateAdd('m',0,"11/01/1975 11:59 PM")#x");
				assertEquals("{ts '1975-11-01 12:00:00'}x","#DateAdd('m',0,"11/01/1975 12:00 PM")#x");
				assertEquals("{ts '1975-11-01 12:01:00'}x","#DateAdd('m',0,"11/01/1975 12:01 PM")#x");
			});
		});
	}
}

