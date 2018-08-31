<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	
	<cfscript>
	
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

	</cfscript>



	<cffunction name="testLSDateFormatClassic" localMode="modern">

<!--- begin old test code --->
<cfset orgLocale=getLocale()>
<cfset setLocale("German (Swiss)")>
<cfset dt=CreateDateTime(2004,1,2,4,5,6)>

<cfset valueEquals(left="#lsdateFormat(dt)#", right="02.01.2004")>
<cfset valueEquals(left="#lsdateFormat(dt,"short")#x", right="02.01.04x")>
<cfset valueEquals(left="#lsdateFormat(dt,"medium")#x", right="02.01.2004x")>
<cfset valueEquals(left="#lsdateFormat(dt,"long")#x", right="2. Januar 2004x")>
<cfset valueEquals(left="#lsdateFormat(dt,"full")#x", right="Freitag, 2. Januar 2004x")>
<cfset valueEquals(left="#lsdateFormat(dt,"full")#x", right="Freitag, 2. Januar 2004x")>


<cfset valueEquals(left="#lsdateFormat(dt,"yyyy")#", right="2004")>
<cfset valueEquals(left="#lsdateFormat(dt,"yy")#", right="04")>
<cfset valueEquals(left="#lsdateFormat(dt,"y")#", right="2004")>
<cfset valueEquals(left="#lsdateFormat(dt,"YYYY")#", right="2004")>
<cfset valueEquals(left="#lsdateFormat(dt,"YY")#", right="04")>
<cfset valueEquals(left="#lsdateFormat(dt,"Y")#", right="2004")>
<cfset valueEquals(left="#lsdateFormat(dt,"MMMM")#", right="Januar")>

<cfset valueEquals(left="#lsdateFormat(dt,"mmm")#", right="Jan")>
<cfset valueEquals(left="#lsdateFormat(dt,"mm")#x", right="01x")>
<cfset valueEquals(left="#lsdateFormat(dt,"m")#x", right="1x")>
<cfset valueEquals(left="#lsdateFormat(dt,"dddd")#", right="Freitag")>
<cfset valueEquals(left="#lsdateFormat(dt,"ddd")#", right="Fr")>
<cfset valueEquals(left="#lsdateFormat(dt,"dd")#x", right="02x")>
<cfset valueEquals(left="#lsdateFormat(dt,"d")#x", right="2x")>
<cfset valueEquals(left="#lsdateFormat(dt,"dd.mm.yyyy")#x", right="02.01.2004x")>
<cfset valueEquals(left="#lsdateFormat('',"dd.mm.yyyy")#", right="")>

<cfset setLocale(orgLocale)>
<cfset valueEquals(left="31",right="#LSDateFormat(1,"dd")#")>
	
		
	
	
<cfset d=CreateDateTime(2008,4,6,1,2,3)>
	
<cfset setlocale('German (swiss)')>
<cfset valueEquals(left="#lsDateFormat(d,'short')#", right="06.04.08")>
<cfset valueEquals(left="#lsDateFormat('06.04.08','short')#", right="06.04.08")>
<cfset valueEquals(left="#lsDateFormat('06.04.2008','short')#", right="06.04.08")>
<cfset valueEquals(left="#lsDateFormat('6. April 2008','short')#", right="06.04.08")>
<cfset valueEquals(left="#lsDateFormat(d,'medium')#", right="06.04.2008")>
<cfset valueEquals(left="#lsDateFormat('06.04.08','medium')#", right="06.04.2008")>
<cfset valueEquals(left="#lsDateFormat('06.04.2008','medium')#", right="06.04.2008")>
<cfset valueEquals(left="#lsDateFormat('Sonntag, 6. April 2008','medium')#", right="06.04.2008")>
<cfset valueEquals(left="#lsDateFormat(d,'long')#", right="6. April 2008")>
<cfset valueEquals(left="#lsDateFormat('06.04.08','long')#", right="6. April 2008")>
<cfset valueEquals(left="#lsDateFormat('06.04.2008','long')#", right="6. April 2008")>
<cfset valueEquals(left="#lsDateFormat('Sonntag, 6. April 2008','long')#", right="6. April 2008")>
<cfset valueEquals(left="#lsDateFormat(d,'full')#", right="Sonntag, 6. April 2008")>
<cfset valueEquals(left="#lsDateFormat('06.04.08','full')#", right="Sonntag, 6. April 2008")>
<cfset valueEquals(left="#lsDateFormat('06.04.2008','full')#", right="Sonntag, 6. April 2008")>
<cfset valueEquals(left="#lsDateFormat('Sonntag, 6. April 2008','full')#", right="Sonntag, 6. April 2008")>

<!--- only supported by railo --->
<cfset valueEquals(left="#lsDateFormat('Sonntag, 6. April 2008','short')#", right="06.04.08")>
<cfset valueEquals(left="#lsDateFormat('6. April 2008','medium')#", right="06.04.2008")>


<cfscript>
	setlocale('french (standard)');

	if(getJavaVersion()>=10) {
		assertEquals("06/04/2008",lsDateFormat(d,'short'));
		assertEquals("06/04/2008",lsDateFormat(('06/04/08'),'short'));
		assertEquals("06/04/2008",lsDateFormat('6 avr. 2008','short'));
		assertEquals("06/04/2008",lsDateFormat('6 avril 2008','short'));
		assertEquals("06/04/2008",lsDateFormat('dimanche 6 avril 2008','short'));

	}
	else {
		assertEquals("06/04/08",lsDateFormat(d,'short'));
		assertEquals("06/04/08",lsDateFormat(('06/04/08'),'short'));
		assertEquals("06/04/08",lsDateFormat('6 avr. 2008','short'));
		assertEquals("06/04/08",lsDateFormat('6 avril 2008','short'));
		assertEquals("06/04/08",lsDateFormat('dimanche 6 avril 2008','short'));
	}
	


</cfscript>

<cfset valueEquals(left="#lsDateFormat(d,'medium')#", right="6 avr. 2008")>
<cfset valueEquals(left="#lsDateFormat('06/04/08','medium')#", right="6 avr. 2008")>
<cfset valueEquals(left="#lsDateFormat('6 avr. 2008','medium')#", right="6 avr. 2008")>
<cfset valueEquals(left="#lsDateFormat('6 avril 2008','medium')#", right="6 avr. 2008")>
<cfset valueEquals(left="#lsDateFormat('dimanche 6 avril 2008','medium')#", right="6 avr. 2008")>

<cfset valueEquals(left="#lsDateFormat(d,'long')#", right="6 avril 2008")>
<cfset valueEquals(left="#lsDateFormat('06/04/08','long')#", right="6 avril 2008")>
<cfset valueEquals(left="#lsDateFormat('6 avr. 2008','long')#", right="6 avril 2008")>
<cfset valueEquals(left="#lsDateFormat('6 avril 2008','long')#", right="6 avril 2008")>
<cfset valueEquals(left="#lsDateFormat('dimanche 6 avril 2008','long')#", right="6 avril 2008")>

<cfset valueEquals(left="#lsDateFormat(d,'full')#", right="dimanche 6 avril 2008")>
<cfset valueEquals(left="#lsDateFormat('06/04/08','full')#", right="dimanche 6 avril 2008")>
<cfset valueEquals(left="#lsDateFormat('6 avr. 2008','full')#", right="dimanche 6 avril 2008")>
<cfset valueEquals(left="#lsDateFormat('6 avril 2008','full')#", right="dimanche 6 avril 2008")>
<cfset valueEquals(left="#lsDateFormat('dimanche 6 avril 2008','full')#", right="dimanche 6 avril 2008")>

<cfset setlocale('English (US)')>
<cfset valueEquals(left="#lsDateFormat(d,'short')#", right="4/6/08")>
<cfset valueEquals(left="#lsDateFormat('4/6/08','short')#", right="4/6/08")>
<cfset valueEquals(left="#lsDateFormat('Apr 6, 2008','short')#", right="4/6/08")>
<cfset valueEquals(left="#lsDateFormat('April 6, 2008','short')#", right="4/6/08")>
<cfset valueEquals(left="#lsDateFormat('Sunday, April 6, 2008','short')#", right="4/6/08")>
<cfset valueEquals(left="#lsDateFormat(d,'medium')#", right="Apr 6, 2008")>
<cfset valueEquals(left="#lsDateFormat('4/6/08','medium')#", right="Apr 6, 2008")>
<cfset valueEquals(left="#lsDateFormat('Apr 6, 2008','medium')#", right="Apr 6, 2008")>
<cfset valueEquals(left="#lsDateFormat('April 6, 2008','medium')#", right="Apr 6, 2008")>
<cfset valueEquals(left="#lsDateFormat('Sunday, April 6, 2008','medium')#", right="Apr 6, 2008")>
<cfset valueEquals(left="#lsDateFormat(d,'long')#", right="April 6, 2008")>
<cfset valueEquals(left="#lsDateFormat('4/6/08','long')#", right="April 6, 2008")>
<cfset valueEquals(left="#lsDateFormat('Apr 6, 2008','long')#", right="April 6, 2008")>
<cfset valueEquals(left="#lsDateFormat('April 6, 2008','long')#", right="April 6, 2008")>
<cfset valueEquals(left="#lsDateFormat('Sunday, April 6, 2008','long')#", right="April 6, 2008")>
<cfset valueEquals(left="#lsDateFormat(d,'full')#", right="Sunday, April 6, 2008")>
<cfset valueEquals(left="#lsDateFormat('4/6/08','full')#", right="Sunday, April 6, 2008")>
<cfset valueEquals(left="#lsDateFormat('Apr 6, 2008','full')#", right="Sunday, April 6, 2008")>
<cfset valueEquals(left="#lsDateFormat('April 6, 2008','full')#", right="Sunday, April 6, 2008")>
<cfset valueEquals(left="#lsDateFormat('Sunday, April 6, 2008','full')#", right="Sunday, April 6, 2008")>
<cfscript>
	setlocale('English (UK)');

	if(getJavaVersion()>=10) {
		shortResult="06/04/2008";
		medResult="6 Apr 2008";
		longResult="6 April 2008";
	}
	else {
		shortResult="06/04/08";
		medResult="06-Apr-2008";
		longResult="06 April 2008";
	}
	


</cfscript>


<cfset valueEquals(left="#lsDateFormat(d,'short')#", right=shortResult)>
<cfset valueEquals(left="#lsDateFormat('06/04/08','short')#", right=shortResult)>
<cfset valueEquals(left="#lsDateFormat('06-Apr-2008','short')#", right=shortResult)>
<cfset valueEquals(left="#lsDateFormat('06 April 2008','short')#", right=shortResult)>
<cfset valueEquals(left="#lsDateFormat('Sunday, 6 April 2008','short')#", right=shortResult	)>

<cfset valueEquals(left="#lsDateFormat(d,'medium')#", right=medResult)>
<cfset valueEquals(left="#lsDateFormat('06/04/08','medium')#", right=medResult)>
<cfset valueEquals(left="#lsDateFormat('06-Apr-2008','medium')#", right=medResult)>
<cfset valueEquals(left="#lsDateFormat('06 April 2008','medium')#", right=medResult)>
<cfset valueEquals(left="#lsDateFormat('Sunday, 6 April 2008','medium')#", right=medResult)>

<cfset valueEquals(left="#lsDateFormat(d,'long')#", right=longResult)>
<cfset valueEquals(left="#lsDateFormat('06/04/08','long')#", right=longResult)>
<cfset valueEquals(left="#lsDateFormat('06-Apr-2008','long')#", right=longResult)>
<cfset valueEquals(left="#lsDateFormat('06 April 2008','long')#", right=longResult)>
<cfset valueEquals(left="#lsDateFormat('Sunday, 6 April 2008','long')#", right=longResult)>

<cfset valueEquals(left="#lsDateFormat(d,'full')#", right="Sunday, 6 April 2008")>
<cfset valueEquals(left="#lsDateFormat('06/04/08','full')#", right="Sunday, 6 April 2008")>
<cfset valueEquals(left="#lsDateFormat('06-Apr-2008','full')#", right="Sunday, 6 April 2008")>
<cfset valueEquals(left="#lsDateFormat('06 April 2008','full')#", right="Sunday, 6 April 2008")>
<cfset valueEquals(left="#lsDateFormat('Sunday, 6 April 2008','full')#", right="Sunday, 6 April 2008")>

<cfset testDate = "2009-06-13 17:04:06" />
<cfset setLocale("Dutch (Standard)")>
<cfset valueEquals(left="#lsDateFormat(testDate, 'dddd d mmmm yyyy')#", right="zaterdag 13 juni 2009")>
<cfset valueEquals(left="#lsDateFormat(parseDateTime(testDate), 'dddd d mmmm yyyy')#", right="zaterdag 13 juni 2009")>

<cfset setLocale("german (swiss)")>
<cfset valueEquals(left="#lsDateFormat(testDate, 'dddd d mmmm yyyy')#", right="Samstag 13 Juni 2009")>
<cfset valueEquals(left="#lsDateFormat(parseDateTime(testDate), 'dddd d mmmm yyyy')#", right="Samstag 13 Juni 2009")>


<cfset setLocale(orgLocale)>

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>

<cfscript>

	private function getJavaVersion() {
	    var raw=server.java.version;
	    var arr=listToArray(raw,'.');
	    if(arr[1]==1) // version 1-9
	        return arr[2];
	    return arr[1];
	}

</cfscript>

</cfcomponent>