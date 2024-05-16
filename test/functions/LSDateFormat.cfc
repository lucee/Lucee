component extends="org.lucee.cfml.test.LuceeTestCase"{	
	function beforeAll(){
		setLocale("en_us");
	}

	function afterAll(){
		setLocale("en_us");
	}

	function testMemberFunction(){
		local.orgLocale=getLocale();
		setLocale("German (Swiss)");
		setTimeZone('CET');
		dt=CreateDateTime(2004,1,2,14,5,6);
		try{
			assertEquals("02.01.2004",dt.lsdateFormat());
			assertEquals("02.01.04",dt.lsdateFormat("short"));
			assertEquals("2004",dt.lsdateFormat("yyyy"));
			assertEquals("Jan 2, 2004",dt.lsdateFormat(locale:"en_us"));
			assertEquals("Jan 2, 2004",dt.lsdateFormat(locale:"en_us",timezone:"CET"));
		}
		finally {
			setLocale(orgLocale);
		}
	}
 
	function testLuceeMemberFunction(){
		local.testcase=new LSDateFormat.LSDateFormat();
		testcase.testMemberFunction();
	}
	function testLuceeFunction(){
		local.testcase=new LSDateFormat.LSDateFormat();
		testcase.testFunction();
	}


	function run( testResults , testBox ) {
		describe( "test case for LSDateFormat", function() {
			it(title = "Checking with LSDateFormat", body = function( currentSpec ) {
				orgLocale=getLocale();
				setLocale("German (Swiss)");
				dt=CreateDateTime(2004,1,2,4,5,6);

				assertEquals("02.01.2004", "#lsdateFormat(dt)#");
				assertEquals("02.01.04x", "#lsdateFormat(dt,"short")#x");
				assertEquals("02.01.2004x", "#lsdateFormat(dt,"medium")#x");
				assertEquals("2. Januar 2004x", "#lsdateFormat(dt,"long")#x");
				assertEquals("Freitag, 2. Januar 2004x", "#lsdateFormat(dt,"full")#x");
				assertEquals("Freitag, 2. Januar 2004x", "#lsdateFormat(dt,"full")#x");

				assertEquals("2004", "#lsdateFormat(dt,"yyyy")#");
				assertEquals("04", "#lsdateFormat(dt,"yy")#");
				assertEquals("2004", "#lsdateFormat(dt,"y")#");
				assertEquals("2004", "#lsdateFormat(dt,"YYYY")#");
				assertEquals("04", "#lsdateFormat(dt,"YY")#");
				assertEquals("2004", "#lsdateFormat(dt,"Y")#");
				assertEquals("Januar", "#lsdateFormat(dt,"MMMM")#");

				assertEquals("Jan", "#lsdateFormat(dt,"mmm")#");
				assertEquals("01x", "#lsdateFormat(dt,"mm")#x");
				assertEquals("1x", "#lsdateFormat(dt,"m")#x");
				assertEquals("Freitag", "#lsdateFormat(dt,"dddd")#");
				assertEquals("Fr", "#lsdateFormat(dt,"ddd")#");
				assertEquals("02x", "#lsdateFormat(dt,"dd")#x");
				assertEquals("2x", "#lsdateFormat(dt,"d")#x");
				assertEquals("02.01.2004x", "#lsdateFormat(dt,"dd.mm.yyyy")#x");
				assertEquals("", "#lsdateFormat('',"dd.mm.yyyy")#");

				setLocale(orgLocale);
				assertEquals("31","#LSDateFormat(1,"dd")#");
						
				d=CreateDateTime(2008,4,6,1,2,3);
					
				setlocale('German (swiss)');
				assertEquals("06.04.08", "#lsDateFormat(d,'short')#");
				assertEquals("06.04.08", "#lsDateFormat('06.04.08','short')#");
				assertEquals("06.04.08", "#lsDateFormat('06.04.2008','short')#");
				assertEquals("06.04.08", "#lsDateFormat('6. April 2008','short')#");
				assertEquals("06.04.2008", "#lsDateFormat(d,'medium')#");
				assertEquals("06.04.2008", "#lsDateFormat('06.04.08','medium')#");
				assertEquals("06.04.2008", "#lsDateFormat('06.04.2008','medium')#");
				assertEquals("06.04.2008", "#lsDateFormat('Sonntag, 6. April 2008','medium')#");
				assertEquals("6. April 2008", "#lsDateFormat(d,'long')#");
				assertEquals("6. April 2008", "#lsDateFormat('06.04.08','long')#");
				assertEquals("6. April 2008", "#lsDateFormat('06.04.2008','long')#");
				assertEquals("6. April 2008", "#lsDateFormat('Sonntag, 6. April 2008','long')#");
				assertEquals("Sonntag, 6. April 2008", "#lsDateFormat(d,'full')#");
				assertEquals("Sonntag, 6. April 2008", "#lsDateFormat('06.04.08','full')#");
				assertEquals("Sonntag, 6. April 2008", "#lsDateFormat('06.04.2008','full')#");
				assertEquals("Sonntag, 6. April 2008", "#lsDateFormat('Sonntag, 6. April 2008','full')#");

				<!--- only supported by railo --->
				assertEquals("06.04.08", "#lsDateFormat('Sonntag, 6. April 2008','short')#");
				assertEquals("06.04.2008", "#lsDateFormat('6. April 2008','medium')#");

				setlocale('french (standard)');
				if(getJavaVersion()>=9) {
					assertEquals("06/04/2008", "#lsDateFormat(d,'short')#");
					assertEquals("06/04/2008", "#lsDateFormat(('06/04/08'),'short')#");
					assertEquals("06/04/2008", "#lsDateFormat('6 avr. 2008','short')#");
					assertEquals("06/04/2008", "#lsDateFormat('6 avril 2008','short')#");
					assertEquals("06/04/2008", "#lsDateFormat('dimanche 6 avril 2008','short')#");
				}
				else {
					assertEquals("06/04/08", "#lsDateFormat(d,'short')#");
					assertEquals("06/04/08", "#lsDateFormat(('06/04/08'),'short')#");
					assertEquals("06/04/08", "#lsDateFormat('6 avr. 2008','short')#");
					assertEquals("06/04/08", "#lsDateFormat('6 avril 2008','short')#");
					assertEquals("06/04/08", "#lsDateFormat('dimanche 6 avril 2008','short')#");
				}
				
				assertEquals("6 avr. 2008", "#lsDateFormat(d,'medium')#");
				assertEquals("6 avr. 2008", "#lsDateFormat('06/04/08','medium')#");
				assertEquals("6 avr. 2008", "#lsDateFormat('6 avr. 2008','medium')#");
				assertEquals("6 avr. 2008", "#lsDateFormat('6 avril 2008','medium')#");
				assertEquals("6 avr. 2008", "#lsDateFormat('dimanche 6 avril 2008','medium')#");
				assertEquals("6 avril 2008", "#lsDateFormat(d,'long')#");
				assertEquals("6 avril 2008", "#lsDateFormat('06/04/08','long')#");
				assertEquals("6 avril 2008", "#lsDateFormat('6 avr. 2008','long')#");
				assertEquals("6 avril 2008", "#lsDateFormat('6 avril 2008','long')#");
				assertEquals("6 avril 2008", "#lsDateFormat('dimanche 6 avril 2008','long')#");
				assertEquals("dimanche 6 avril 2008", "#lsDateFormat(d,'full')#");
				assertEquals("dimanche 6 avril 2008", "#lsDateFormat('06/04/08','full')#");
				assertEquals("dimanche 6 avril 2008", "#lsDateFormat('6 avr. 2008','full')#");
				assertEquals("dimanche 6 avril 2008", "#lsDateFormat('6 avril 2008','full')#");
				assertEquals("dimanche 6 avril 2008", "#lsDateFormat('dimanche 6 avril 2008','full')#");

				setlocale('English (US)');
				assertEquals("4/6/08", "#lsDateFormat(d,'short')#");
				assertEquals("4/6/08", "#lsDateFormat('4/6/08','short')#");
				assertEquals("4/6/08", "#lsDateFormat('Apr 6, 2008','short')#");
				assertEquals("4/6/08", "#lsDateFormat('April 6, 2008','short')#");
				assertEquals("4/6/08", "#lsDateFormat('Sunday, April 6, 2008','short')#");
				assertEquals("Apr 6, 2008", "#lsDateFormat(d,'medium')#");
				assertEquals("Apr 6, 2008", "#lsDateFormat('4/6/08','medium')#");
				assertEquals("Apr 6, 2008", "#lsDateFormat('Apr 6, 2008','medium')#");
				assertEquals("Apr 6, 2008", "#lsDateFormat('April 6, 2008','medium')#");
				assertEquals("Apr 6, 2008", "#lsDateFormat('Sunday, April 6, 2008','medium')#");
				assertEquals("April 6, 2008", "#lsDateFormat(d,'long')#");
				assertEquals("April 6, 2008", "#lsDateFormat('4/6/08','long')#");
				assertEquals("April 6, 2008", "#lsDateFormat('Apr 6, 2008','long')#");
				assertEquals("April 6, 2008", "#lsDateFormat('April 6, 2008','long')#");
				assertEquals("April 6, 2008", "#lsDateFormat('Sunday, April 6, 2008','long')#");
				assertEquals("Sunday, April 6, 2008", "#lsDateFormat(d,'full')#");
				assertEquals("Sunday, April 6, 2008", "#lsDateFormat('4/6/08','full')#");
				assertEquals("Sunday, April 6, 2008", "#lsDateFormat('Apr 6, 2008','full')#");
				assertEquals("Sunday, April 6, 2008", "#lsDateFormat('April 6, 2008','full')#");
				assertEquals("Sunday, April 6, 2008", "#lsDateFormat('Sunday, April 6, 2008','full')#");

				setlocale('English (UK)');
				if(getJavaVersion()>=9) {
					assertEquals("06/04/2008", "#lsDateFormat(d,'short')#");
					assertEquals("06/04/2008", "#lsDateFormat('06/04/08','short')#");
					assertEquals("06/04/2008", "#lsDateFormat('06-Apr-2008','short')#");
					assertEquals("06/04/2008", "#lsDateFormat('06 April 2008','short')#");
					assertEquals("06/04/2008", "#lsDateFormat('Sunday, 6 April 2008','short')#");

					assertEquals("6 Apr 2008", "#lsDateFormat(d,'medium')#");
					assertEquals("6 Apr 2008", "#lsDateFormat('06/04/08','medium')#");
					assertEquals("6 Apr 2008", "#lsDateFormat('06-Apr-2008','medium')#");
					assertEquals("6 Apr 2008", "#lsDateFormat('06 April 2008','medium')#");
					assertEquals("6 Apr 2008", "#lsDateFormat('Sunday, 6 April 2008','medium')#");
					
					assertEquals("6 April 2008", "#lsDateFormat(d,'long')#");
					assertEquals("6 April 2008", "#lsDateFormat('06/04/08','long')#");
					assertEquals("6 April 2008", "#lsDateFormat('06-Apr-2008','long')#");
					assertEquals("6 April 2008", "#lsDateFormat('06 April 2008','long')#");
					assertEquals("6 April 2008", "#lsDateFormat('Sunday, 6 April 2008','long')#");
				}
				else {
					assertEquals("06/04/08", "#lsDateFormat(d,'short')#");
					assertEquals("06/04/08", "#lsDateFormat('06/04/08','short')#");
					assertEquals("06/04/08", "#lsDateFormat('06-Apr-2008','short')#");
					assertEquals("06/04/08", "#lsDateFormat('06 April 2008','short')#");
					assertEquals("06/04/08", "#lsDateFormat('Sunday, 6 April 2008','short')#");

					assertEquals("06-Apr-2008", "#lsDateFormat(d,'medium')#");
					assertEquals("06-Apr-2008", "#lsDateFormat('06/04/08','medium')#");
					assertEquals("06-Apr-2008", "#lsDateFormat('06-Apr-2008','medium')#");
					assertEquals("06-Apr-2008", "#lsDateFormat('06 April 2008','medium')#");
					assertEquals("06-Apr-2008", "#lsDateFormat('Sunday, 6 April 2008','medium')#");

					assertEquals("06 April 2008", "#lsDateFormat(d,'long')#");
					assertEquals("06 April 2008", "#lsDateFormat('06/04/08','long')#");
					assertEquals("06 April 2008", "#lsDateFormat('06-Apr-2008','long')#");
					assertEquals("06 April 2008", "#lsDateFormat('06 April 2008','long')#");
					assertEquals("06 April 2008", "#lsDateFormat('Sunday, 6 April 2008','long')#");
				}
				assertEquals("Sunday, 6 April 2008", "#lsDateFormat(d,'full')#");
				assertEquals("Sunday, 6 April 2008", "#lsDateFormat('06/04/08','full')#");
				assertEquals("Sunday, 6 April 2008", "#lsDateFormat('06-Apr-2008','full')#");
				assertEquals("Sunday, 6 April 2008", "#lsDateFormat('06 April 2008','full')#");
				assertEquals("Sunday, 6 April 2008", "#lsDateFormat('Sunday, 6 April 2008','full')#");

				testDate = "2009-06-13 17:04:06";
				setLocale("Dutch (Standard)");
				assertEquals("zaterdag 13 juni 2009", "#lsDateFormat(testDate, 'dddd d mmmm yyyy')#");
				assertEquals("zaterdag 13 juni 2009", "#lsDateFormat(parseDateTime(testDate), 'dddd d mmmm yyyy')#");

				setLocale("german (swiss)");
				assertEquals("Samstag 13 Juni 2009", "#lsDateFormat(testDate, 'dddd d mmmm yyyy')#");
				assertEquals("Samstag 13 Juni 2009", "#lsDateFormat(parseDateTime(testDate), 'dddd d mmmm yyyy')#");

				setLocale(orgLocale);
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

