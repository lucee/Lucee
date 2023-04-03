component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for Day", function() {
			it(title = "Checking with Day", body = function( currentSpec ) {
				d1=CreateDateTime(2001, 12, 1, 4, 10, 1);
				assertEquals("1", "#day(d1)#");

				assertEquals("#Day(1)#", "31" );

				x=struct();
				x.sDate = "11/01/2009";
				l=GetLocale();
				SetLocale("portuguese (brazilian)");
				assertEquals("#LsParseDateTime(x.sDate)#x", "{ts '2009-01-11 00:00:00'}x");
				assertEquals("#ParseDateTime(x.sDate)#x", "{ts '2009-11-01 00:00:00'}x");
				x.sNewDate = LsDateFormat(LsParseDateTime(x.sDate), 'dd/mm/yyyy');
				assertEquals("#x.sNewDate#x", "11/01/2009x");
				assertEquals("#Day(LSParseDateTime(x.sNewDate))#", "11");
				assertEquals("#Day(ParseDateTime(x.sNewDate))#", "1");
				assertEquals("#Day(x.sNewDate)#", "1");
				assertEquals("#Month(x.sNewDate)#", "11");
				assertEquals("#Month(ParseDateTime(x.sNewDate))#", "11");
				assertEquals("#Month(LsParseDateTime(x.sNewDate))#", "1");
				SetLocale(l);
			});
			it(title = "Checking day() member function", body = function( currentSpec ) {
				d1 = createDateTime(2001, 12, 1, 4, 10, 1);
				assertEquals("1", "#d1.day()#");
				x = struct();
				x.sDate = "11/01/2009";
				l = getLocale();
				setLocale("portuguese (brazilian)");
				assertEquals("#lsParseDateTime(x.sDate)#x", "{ts '2009-01-11 00:00:00'}x");
				assertEquals("#parseDateTime(x.sDate)#x", "{ts '2009-11-01 00:00:00'}x");
				x.sNewDate = lsDateFormat(lsParseDateTime(x.sDate), 'dd/mm/yyyy');
				assertEquals("#x.sNewDate#x", "11/01/2009x");
				assertEquals("#lsParseDateTime(x.sNewDate).day()#", "11");
				assertEquals("#parseDateTime(x.sNewDate).day()#", "1");
				setLocale(l);
			});
		});	
	}
}