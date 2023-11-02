component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LSparseDateTime()", body=function() {
			it(title="checking LSparseDateTime() function", body = function( currentSpec ) {


				orgLocale=getLocale();
				setTimeZone("CET");
				assertEquals("{ts '2008-12-31 00:00:00'}" , "#lsParseDateTime( "12/31/2008",'english (us)')#");
				assertEquals("{ts '2008-12-31 00:00:00'}" , "#lsParseDateTime( "31/12/2008",'english (uk)')#");
				assertEquals("{ts '2008-12-31 00:00:00'}", "#lsParseDateTime( "31.12.2008",'german (standard)')#");

				try {
					assertEquals("{ts '2008-12-31 00:00:00'}", "#lsParseDateTime( "12/31/2008",'english (uk)')#");
					fail("must throw:is an invalid date or time string");
				} catch ( any e){}

				try {
					assertEquals("{ts '2008-12-31 00:00:00'}", "#lsParseDateTime( "12.31.2008",'german (standard)')#");
					fail("must throw:is an invalid date or time string");
				} catch ( any e){}

				try {
					assertEquals("{ts '2008-12-31 00:00:00'}", "#lsParseDateTime( "12/31/2008",'english (uk)')#");
					fail("must throw:is an invalid date or time string");
				} catch ( any e){}



				assertEquals("{ts '2008-12-30 00:00:00'}", "#lsParseDateTime( "12/30/08",'english (us)')#");
				assertEquals("{ts '2008-02-29 00:00:00'}", "#lsParseDateTime( "Feb 29, 2008",'english (us)')#");
				assertEquals("{ts '2008-02-29 00:00:00'}", "#lsParseDateTime( "February 29, 2008",'english (us)')#");
				assertEquals("{ts '2008-02-29 00:00:00'}", "#lsParseDateTime( "Friday, February 29, 2008",'english (us)')#");


				try {
				assertEquals("#false#", "#lsParseDateTime( "13/30/08",'english (us)')#");
				fail("must throw:is an invalid date or time string");
				} catch ( any e){}
				try {
				assertEquals("#false#", "#lsParseDateTime( "Feb 30, 2008",'english (us)')#");
				fail("must throw:is an invalid date or time string");
				} catch ( any e){}
				try {
				assertEquals("#false#", "#lsParseDateTime( "February 30, 2008",'english (us)')#");
				fail("must throw:is an invalid date or time string");
				} catch ( any e){}
				try {
				assertEquals("#false#", "#lsParseDateTime( "Monday, February 29, 2008",'english (us)')#");
				fail("must throw:is an invalid date or time string");
				} catch ( any e){}



				assertEquals("{ts '2008-02-29 00:00:00'}", "#lsParseDateTime( "29-Feb-2008",'english (uk)')#");
				assertEquals("{ts '2008-02-29 00:00:00'}", "#lsParseDateTime( "29 February 2008",'english (uk)')#");
				assertEquals("{ts '2008-02-29 00:00:00'}", "#lsParseDateTime( "Friday, 29 February 2008",'english (uk)')#");
				try {
				assertEquals("#false#", "#lsParseDateTime( "30-Feb-2008",'english (uk)')#");
				fail("must throw:is an invalid date or time string");
				} catch ( any e){}
				try {
				assertEquals("#false#", "#lsParseDateTime( "30 February 2008",'english (uk)')#");
				fail("must throw:is an invalid date or time string");
				} catch ( any e){}
				try {
				assertEquals("#false#", "#lsParseDateTime( "Monday, 29 February 2008",'english (uk)')#");
				fail("must throw:is an invalid date or time string");
				} catch ( any e){}


				assertEquals("{ts '2008-02-29 00:00:00'}", "#lsParseDateTime( "29. Februar 2008",'german (swiss)')#");
				assertEquals("{ts '2008-02-29 00:00:00'}", "#lsParseDateTime( "Freitag, 29. Februar 2008",'german (swiss)')#");
				try {
				assertEquals("#false#", "#lsParseDateTime( "30. Februar 2008",'german (swiss)')#");
				fail("must throw:is an invalid date or time string");
				} catch ( any e){}
				try {
				assertEquals("#false#", "#lsParseDateTime( "Montag, 29. Februar 2008",'german (swiss)')#");
				fail("must throw:is an invalid date or time string");
				} catch ( any e){}



				winter=CreateDateTime(2008,1,6,1,2,3);
				summer=CreateDateTime(2008,6,6,1,2,3);


				assertEquals("-{ts '2008-06-04 00:00:00'}", "-#lsParseDateTime("4/6/2008","english (UK)")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("4/6/2008","english (US)")#");

				setlocale('German (swiss)');
				assertEquals("-{ts '1899-12-30 01:02:03'}", "-#lsParseDateTime("01:02:03 MEZ")#");
				assertEquals("-{ts '1899-12-30 00:02:03'}", "-#lsParseDateTime("01:02:03 MESZ")#");
				assertEquals("-{ts '1899-12-30 02:02:03'}", "-#lsParseDateTime("01:02:03 GMT")#");

				assertEquals("-{ts '2008-02-06 01:02:01'}", "-#lsParseDateTime("06.02.2008 01:02:01 MEZ")#");
				assertEquals("-{ts '2008-06-06 01:02:02'}", "-#lsParseDateTime("06.06.2008 01:02:02 MESZ")#");
				assertEquals("-{ts '2008-02-06 00:02:03'}", "-#lsParseDateTime("06.02.2008 01:02:03 MESZ")#");
				assertEquals("-{ts '2008-06-06 01:02:04'}", "-#lsParseDateTime("06.06.2008 01:02:04 MESZ")#");

				assertEquals("-{ts '1899-12-30 00:02:03'}", "-#lsParseDateTime("01:02:03 MESZ")#");

				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("06.04.08")#");
				assertEquals("-{ts '1899-12-30 01:02:00'}", "-#lsParseDateTime("01:02")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("06.04.2008")#");
				assertEquals("-{ts '1899-12-30 01:02:03'}", "-#lsParseDateTime("01:02:03")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("6. April 2008")#");
				assertEquals("-{ts '1899-12-30 00:02:03'}", "-#lsParseDateTime("01:02:03 MESZ")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("Sonntag, 6. April 2008")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("06.04.08 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06.04.08 01:02:03")#");
				if(getJavaVersion()==8) {
					assertEquals("-{ts '2008-04-06 00:02:00'}", "-#lsParseDateTime("06.04.08 1:02 Uhr MESZ")#");
					assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("06.04.08 1:02 Uhr MEZ")#");
					assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("06.04.08 1:02 Uhr MEZ")#");
				}
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("06.04.2008 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06.04.2008 01:02:03")#");
				assertEquals("-{ts '2008-01-06 01:02:03'}", "-#lsParseDateTime("06.01.2008 01:02:03 MEZ")#");
				if(getJavaVersion()==8) {
					assertEquals("-{ts '2008-01-06 01:02:00'}", "-#lsParseDateTime("06.01.2008 1:02 Uhr MEZ")#");
					assertEquals("-{ts '2008-01-06 01:02:00'}", "-#lsParseDateTime("06.01.2008 1:02 Uhr MEZ")#");
					assertEquals("-{ts '2008-01-06 00:02:00'}", "-#lsParseDateTime("06.01.2008 1:02 Uhr MESZ")#");
					assertEquals("-{ts '2008-01-06 01:02:00'}", "-#lsParseDateTime("06.01.2008 1:02 Uhr MEZ")#");
					assertEquals("-{ts '2008-01-06 01:02:00'}", "-#lsParseDateTime("06.01.2008 1:02 Uhr MEZ")#");
					assertEquals("-{ts '2008-06-06 01:02:00'}", "-#lsParseDateTime("06.06.2008 1:02 Uhr MESZ")#");
					assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("6. April 2008 1:02 Uhr MESZ")#");
					assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("Sonntag, 6. April 2008 1:02 Uhr MESZ")#");
					assertEquals("-{ts '1899-12-30 11:02:00'}", "-#lsParseDateTime("11:02 Uhr MEZ")#");
				}
				assertEquals("-{ts '2008-01-06 00:02:03'}", "-#lsParseDateTime("06.01.2008 01:02:03 MESZ")#");
				assertEquals("-{ts '2008-06-06 01:02:03'}", "-#lsParseDateTime("06.06.2008 01:02:03 MESZ")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("6. April 2008 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("6. April 2008 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("6. April 2008 01:02:03 MESZ")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("Sonntag, 6. April 2008 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("Sonntag, 6. April 2008 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("Sonntag, 6. April 2008 01:02:03 MESZ")#");


				setlocale('french (swiss)');
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("06.04.08")#");
				assertEquals("-{ts '1899-12-30 01:02:00'}", "-#lsParseDateTime("01:02")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("6 avr. 2008")#");
				assertEquals("-{ts '1899-12-30 01:02:03'}", "-#lsParseDateTime("01:02:03")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("6. avril 2008")#");
				assertEquals("-{ts '1899-12-30 00:02:03'}", "-#lsParseDateTime("01:02:03 CEST")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("dimanche, 6. avril 2008")#");
				if(getJavaVersion()==8) {
					assertEquals("-{ts '1899-12-30 00:02:00'}", "-#lsParseDateTime("01.02. h CEST")#");
					assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("06.04.08 01.02. h CEST")#");
					assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("6 avr. 2008 01.02. h CEST")#");
					assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("6. avril 2008 01.02. h CEST")#");
					assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("dimanche, 6. avril 2008 01.02. h CEST")#");
				}
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("06.04.08 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06.04.08 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06.04.08 01:02:03 CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("6 avr. 2008 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("6 avr. 2008 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("6 avr. 2008 01:02:03 CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("6. avril 2008 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("6. avril 2008 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("6. avril 2008 01:02:03 CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("dimanche, 6. avril 2008 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("dimanche, 6. avril 2008 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("dimanche, 6. avril 2008 01:02:03 CEST")#");
				setlocale('italian (swiss)');
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("06.04.08")#");
				assertEquals("-{ts '1899-12-30 01:02:00'}", "-#lsParseDateTime("01:02")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("6-apr-2008")#");
				assertEquals("-{ts '1899-12-30 01:02:03'}", "-#lsParseDateTime("01:02:03")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("6. aprile 2008")#");
				assertEquals("-{ts '1899-12-30 00:02:03'}", "-#lsParseDateTime("01:02:03 CEST")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("domenica, 6. aprile 2008")#");
				if(getJavaVersion()==8) {
					assertEquals("-{ts '1899-12-30 00:02:00'}", "-#lsParseDateTime("1.02 h CEST")#");
					assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("06.04.08 1.02 h CEST")#");
					assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("6-apr-2008 1.02 h CEST")#");
					assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("6. aprile 2008 1.02 h CEST")#");
					assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("domenica, 6. aprile 2008 1.02 h CEST")#");
				}
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("06.04.08 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06.04.08 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06.04.08 01:02:03 CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("6-apr-2008 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("6-apr-2008 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("6-apr-2008 01:02:03 CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("6. aprile 2008 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("6. aprile 2008 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("6. aprile 2008 01:02:03 CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("domenica, 6. aprile 2008 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("domenica, 6. aprile 2008 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("domenica, 6. aprile 2008 01:02:03 CEST")#");
				setlocale('English (US)');
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("4/6/08")#");
				assertEquals("-{ts '1899-12-30 01:02:00'}", "-#lsParseDateTime("1:02 AM")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("Apr 6, 2008")#");
				assertEquals("-{ts '1899-12-30 01:02:03'}", "-#lsParseDateTime("1:02:03 AM")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("April 6, 2008")#");
				assertEquals("-{ts '1899-12-30 00:02:03'}", "-#lsParseDateTime("1:02:03 AM CEST")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("Sunday, April 6, 2008")#");
				assertEquals("-{ts '1899-12-30 00:02:03'}", "-#lsParseDateTime("1:02:03 AM CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("4/6/08 1:02 AM")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("4/6/08 1:02:03 AM")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("4/6/08 1:02:03 AM CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("4/6/08 1:02:03 AM CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("Apr 6, 2008 1:02 AM")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("Apr 6, 2008 1:02:03 AM")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("Apr 6, 2008 1:02:03 AM CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("Apr 6, 2008 1:02:03 AM CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("April 6, 2008 1:02 AM")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("April 6, 2008 1:02:03 AM")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("April 6, 2008 1:02:03 AM CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("April 6, 2008 1:02:03 AM CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("Sunday, April 6, 2008 1:02 AM")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("Sunday, April 6, 2008 1:02:03 AM")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("Sunday, April 6, 2008 1:02:03 AM CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("Sunday, April 6, 2008 1:02:03 AM CEST")#");
				setlocale('English (UK)');
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("06/04/08")#");
				assertEquals("-{ts '1899-12-30 01:02:00'}", "-#lsParseDateTime("01:02")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("06-Apr-2008")#");
				assertEquals("-{ts '1899-12-30 01:02:03'}", "-#lsParseDateTime("01:02:03")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("06 April 2008")#");
				assertEquals("-{ts '1899-12-30 00:02:03'}", "-#lsParseDateTime("01:02:03 CEST")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("Sunday, 6 April 2008")#");
				if(getJavaVersion()==8) {
					assertEquals("-{ts '1899-12-30 00:02:03'}", "-#lsParseDateTime("01:02:03 o'clock CEST")#");
					assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06/04/08 01:02:03 o'clock CEST")#");
					assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06-Apr-2008 01:02:03 o'clock CEST")#");
					assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06 April 2008 01:02:03 o'clock CEST")#");
					assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("Sunday, 6 April 2008 01:02:03 o'clock CEST")#");
				}
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("06/04/08 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06/04/08 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06/04/08 01:02:03 CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("06-Apr-2008 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06-Apr-2008 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06-Apr-2008 01:02:03 CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("06 April 2008 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06 April 2008 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06 April 2008 01:02:03 CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("Sunday, 6 April 2008 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("Sunday, 6 April 2008 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("Sunday, 6 April 2008 01:02:03 CEST")#");



				setlocale('english (us)');
				assertEquals("-{ts '1899-12-30 01:02:03'}", "-#lsParseDateTime("01:02:03 AM CET")#");
				assertEquals("-{ts '1899-12-30 02:02:03'}", "-#lsParseDateTime("01:02:03 AM UTC")#");
				assertEquals("-{ts '1899-12-30 05:32:03'}", "-#lsParseDateTime("01:02:03 AM NST")#");

				setlocale('english (uk)');
				if(getJavaVersion()==8) {
					assertEquals("-{ts '2008-06-06 01:02:03'}", "-#lsParseDateTime("06 June 2008 01:02:03 o'clock CEST","english (uk)")#");
					assertEquals("-{ts '2008-02-06 00:02:03'}", "-#lsParseDateTime("06 February 2008 01:02:03 o'clock CEST","english (uk)")#");
					assertEquals("-{ts '2008-06-06 10:02:03'}", "-#lsParseDateTime("06 June 2008 01:02:03 o'clock PDT","english (uk)")#");

					assertEquals("-{ts '2008-04-06 03:02:03'}", "-#lsParseDateTime("06 April 2008 01:02:03 o'clock GMT")#");
					assertEquals("-{ts '2008-04-06 06:32:03'}", "-#lsParseDateTime("06 April 2008 01:02:03 o'clock NST")#");
				}


				setlocale('German (swiss)');
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("06.04.08")#");
				assertEquals("-{ts '1899-12-30 01:02:00'}", "-#lsParseDateTime("01:02")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("06.04.2008")#");
				assertEquals("-{ts '1899-12-30 01:02:03'}", "-#lsParseDateTime("01:02:03")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("6. April 2008")#");
				assertEquals("-{ts '1899-12-30 00:02:03'}", "-#lsParseDateTime("01:02:03 MESZ")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("Sonntag, 6. April 2008")#");
				if(getJavaVersion()==8) {
					assertEquals("-{ts '1899-12-30 01:02:00'}", "-#lsParseDateTime("1:02 Uhr MEZ")#");
					assertEquals("-{ts '2008-04-06 00:02:00'}", "-#lsParseDateTime("06.04.08 1:02 Uhr MESZ")#");
					assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("06.04.2008 1:02 Uhr MESZ")#");
					assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("6. April 2008 1:02 Uhr MESZ")#");
					assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("Sonntag, 6. April 2008 1:02 Uhr MESZ")#");
				}
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("06.04.08 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06.04.08 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("06.04.2008 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06.04.2008 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06.04.2008 01:02:03 MESZ")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("6. April 2008 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("6. April 2008 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("6. April 2008 01:02:03 MESZ")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("Sonntag, 6. April 2008 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("Sonntag, 6. April 2008 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("Sonntag, 6. April 2008 01:02:03 MESZ")#");
				setlocale('french (swiss)');
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("06.04.08")#");
				assertEquals("-{ts '1899-12-30 01:02:00'}", "-#lsParseDateTime("01:02")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("6 avr. 2008")#");
				assertEquals("-{ts '1899-12-30 01:02:03'}", "-#lsParseDateTime("01:02:03")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("6. avril 2008")#");
				assertEquals("-{ts '1899-12-30 00:02:03'}", "-#lsParseDateTime("01:02:03 CEST")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("dimanche, 6. avril 2008")#");
				if(getJavaVersion()==8) {
					assertEquals("-{ts '1899-12-30 00:02:00'}", "-#lsParseDateTime("01.02. h CEST")#");
					assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("06.04.08 01.02. h CEST")#");
					assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("6 avr. 2008 01.02. h CEST")#");
					assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("6. avril 2008 01.02. h CEST")#");
					assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("dimanche, 6. avril 2008 01.02. h CEST")#");
				}
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("06.04.08 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06.04.08 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06.04.08 01:02:03 CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("6 avr. 2008 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("6 avr. 2008 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("6 avr. 2008 01:02:03 CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("6. avril 2008 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("6. avril 2008 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("6. avril 2008 01:02:03 CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("dimanche, 6. avril 2008 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("dimanche, 6. avril 2008 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("dimanche, 6. avril 2008 01:02:03 CEST")#");
				setlocale('italian (swiss)');
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("06.04.08")#");
				assertEquals("-{ts '1899-12-30 01:02:00'}", "-#lsParseDateTime("01:02")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("6-apr-2008")#");
				assertEquals("-{ts '1899-12-30 01:02:03'}", "-#lsParseDateTime("01:02:03")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("6. aprile 2008")#");
				assertEquals("-{ts '1899-12-30 00:02:03'}", "-#lsParseDateTime("01:02:03 CEST")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("domenica, 6. aprile 2008")#");
				if(getJavaVersion()==8) {
					assertEquals("-{ts '1899-12-30 00:02:00'}", "-#lsParseDateTime("1.02 h CEST")#");
					assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("06.04.08 1.02 h CEST")#");
					assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("6-apr-2008 1.02 h CEST")#");
					assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("6. aprile 2008 1.02 h CEST")#");
					assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("domenica, 6. aprile 2008 1.02 h CEST")#");
				}
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("06.04.08 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06.04.08 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06.04.08 01:02:03 CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("6-apr-2008 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("6-apr-2008 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("6-apr-2008 01:02:03 CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("6. aprile 2008 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("6. aprile 2008 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("6. aprile 2008 01:02:03 CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("domenica, 6. aprile 2008 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("domenica, 6. aprile 2008 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("domenica, 6. aprile 2008 01:02:03 CEST")#");

				setlocale('English (US)');
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("4/6/08")#");
				assertEquals("-{ts '1899-12-30 01:02:00'}", "-#lsParseDateTime("1:02 AM")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("Apr 6, 2008")#");
				assertEquals("-{ts '1899-12-30 01:02:03'}", "-#lsParseDateTime("1:02:03 AM")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("April 6, 2008")#");
				assertEquals("-{ts '1899-12-30 00:02:03'}", "-#lsParseDateTime("1:02:03 AM CEST")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("Sunday, April 6, 2008")#");
				assertEquals("-{ts '1899-12-30 00:02:03'}", "-#lsParseDateTime("1:02:03 AM CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("4/6/08 1:02 AM")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("4/6/08 1:02:03 AM")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("4/6/08 1:02:03 AM CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("4/6/08 1:02:03 AM CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("Apr 6, 2008 1:02 AM")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("Apr 6, 2008 1:02:03 AM")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("Apr 6, 2008 1:02:03 AM CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("Apr 6, 2008 1:02:03 AM CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("April 6, 2008 1:02 AM")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("April 6, 2008 1:02:03 AM")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("April 6, 2008 1:02:03 AM CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("April 6, 2008 1:02:03 AM CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("Sunday, April 6, 2008 1:02 AM")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("Sunday, April 6, 2008 1:02:03 AM")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("Sunday, April 6, 2008 1:02:03 AM CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("Sunday, April 6, 2008 1:02:03 AM CEST")#");



				setlocale('English (UK)');
				assertEquals("-{ts '2008-04-06 03:02:03'}", "-#lsParseDateTime("06/04/08 01:02:03 GMT")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06/04/08 01:02:03 CEST")#");
				assertEquals("-{ts '2008-04-06 06:32:03'}", "-#lsParseDateTime("06/04/08 01:02:03 NST")#");


				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("06/04/08")#");
				assertEquals("-{ts '1899-12-30 01:02:00'}", "-#lsParseDateTime("01:02")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("06-Apr-2008")#");
				assertEquals("-{ts '1899-12-30 01:02:03'}", "-#lsParseDateTime("01:02:03")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("06 April 2008")#");
				assertEquals("-{ts '1899-12-30 00:02:03'}", "-#lsParseDateTime("01:02:03 CEST")#");
				assertEquals("-{ts '2008-04-06 00:00:00'}", "-#lsParseDateTime("Sunday, 6 April 2008")#");
				if(getJavaVersion()==8) {
					assertEquals("-{ts '1899-12-30 00:02:03'}", "-#lsParseDateTime("01:02:03 o'clock CEST")#");
					assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06/04/08 01:02:03 o'clock CEST")#");
					assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06-Apr-2008 01:02:03 o'clock CEST")#");
					assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06 April 2008 01:02:03 o'clock CEST")#");
					assertEquals("-{ts '2008-04-06 03:02:03'}", "-#lsParseDateTime("06 April 2008 01:02:03 o'clock GMT")#");
					assertEquals("-{ts '2008-04-06 06:32:03'}", "-#lsParseDateTime("06 April 2008 01:02:03 o'clock NST")#");
					assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("Sunday, 6 April 2008 01:02:03 o'clock CEST")#");
				}
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("06/04/08 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06/04/08 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06/04/08 01:02:03 CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("06-Apr-2008 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06-Apr-2008 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06-Apr-2008 01:02:03 CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("06 April 2008 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06 April 2008 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("06 April 2008 01:02:03 CEST")#");
				assertEquals("-{ts '2008-04-06 01:02:00'}", "-#lsParseDateTime("Sunday, 6 April 2008 01:02")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("Sunday, 6 April 2008 01:02:03")#");
				assertEquals("-{ts '2008-04-06 01:02:03'}", "-#lsParseDateTime("Sunday, 6 April 2008 01:02:03 CEST")#");

				setLocale("German (Swiss)");
				dt=CreateDateTime(2004,1,2,4,5,6);

				

				assertEquals("-{ts '#year(now())#-01-01 00:00:00'}", "-#LSParseDateTime("1/1",'en_us')#");
				cfloop( list="ar_SA,zh_CN,zh_TW,nl_NL,en_AU,en_CA,en_GB,fr_CA,fr_FR,de_DE,iw_IL,hi_IN,it_IT,ja_JP,ko_KR,pt_BR,es_ES,sv_SE,th_TH,th_TH_TH", index="locale"){
					try {
						LSParseDateTime("1/1",locale);
						fail("must throw:#locale# -)> can't cast [1/1] to date value");
					} catch ( any e){}
				}

				setLocale('english (australian)');
				assertEquals("{ts '2010-02-01 00:00:00'}x", "#LSParseDateTime('01/02/2010')#x");
				setLocale(orgLocale);


				str="6014.10";
				assertEquals("true", "#isDate(str)#");
				assertEquals("{ts '6014-10-01 00:00:00'}", "#parseDateTime(str)#");

				try {
					lsparseDateTime(str);
					fail("must throw an error!")
				} catch ( any e){}


				<!--- format --->

				<!--- not supported in CFML <= 9  --->
				assertEquals("-{ts '2002-01-30 07:02:33'}", "-#lsParseDateTime("1/30/02 7:02:33",'en','m/dd/yy h:mm:ss')#");
				assertEquals("-{ts '2002-01-30 07:02:33'}", "-#lsParseDateTime("1/30/02 7:02:33",'en','m/dd/yy h:mm:ss')#");
				assertEquals("-{ts '2002-01-30 07:02:00'}", "-#lsParseDateTime("1/30/2002 7:02 AM",'en','m/dd/yyyy h:mm')#");
				
			});
		});
	}


	private function getJavaVersion() {
        var raw=server.java.version;
        var arr=listToArray(raw,'.');
        if(arr[1]==1) // version 1-9
            return arr[2];
        return arr[1];
    }
}

