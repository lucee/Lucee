component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="checking dateAndTimeFormat()", body=function() {
			it(title='dateAndTimeFormat() function with arguments', body=function( currentSpec ) {
				var d = CreateDateTime(2000,1,2,3,4,5,0,"CET");
				application action="update" timezone="CET";
				setTimeZone("CET");
				assertEquals("02-Jan-2000 03:04:05", DateTimeFormat(d));
				assertEquals("2000.01.02 AD at 03:04:05 CET", DateTimeFormat(d, "yyyy.MM.dd G 'at' HH:nn:ss z"));
				assertEquals("Sun, Jan 2, '00", DateTimeFormat(d, "EEE, MMM d, ''yy"));
				assertEquals("3:04 AM", DateTimeFormat(d, "h:nn a"));
				assertEquals("03 o'clock AM, Central European Time", DateTimeFormat(d, "hh 'o''clock' a, zzzz"));
				assertEquals("3:04 AM, CET", DateTimeFormat(d, "K:nn a, z"));
				assertEquals("02000.January.02 AD 03:04 AM", DateTimeFormat(d, "yyyyy.MMMMM.dd GGG hh:nn aaa"));
				assertEquals("Sun, 2 Jan 2000 03:04:05 +0100", DateTimeFormat(d, "EEE, d MMM yyyy HH:nn:ss Z"));
				assertEquals("000102020405+0000", DateTimeFormat(d, "yyMMddHHnnssZ", "GMT"));
				assertEquals("02-Jan-2000 03:04:05", d.DateTimeFormat());
				assertEquals("2000.01.02 AD at 03:04:05 CET", d.DateTimeFormat("yyyy.MM.dd G 'at' HH:nn:ss z"));
				assertEquals("3:04 AM", d.Format("h:nn a"));
				assertEquals("Sun, 2 Jan 2000 03:04:05 +0100", d.format("EEE, d MMM yyyy HH:nn:ss Z"));
				assertEquals("2000.01.02 03-04", d.format("y.mm.dd hh-nn"));
				assertEquals("2 Jan 2000 03:04:05", d.format("d MMM yyyy HH:nn:ss"));
			});

		});
	}
}