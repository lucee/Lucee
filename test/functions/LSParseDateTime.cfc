<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<!---
	<cffunction name="beforeTests"></cffunction>
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->
	<cffunction name="testLSParseDateTime" localMode="modern">

<!--- begin old test code --->
<cfset orgLocale=getLocale()>


<cfset setTimeZone("CET")>

<cfset valueEquals(left="#lsParseDateTime( "12/31/2008",'english (us)')#", right="{ts '2008-12-31 00:00:00'}")>

<cfset valueEquals(left="#lsParseDateTime( "31/12/2008",'english (uk)')#", right="{ts '2008-12-31 00:00:00'}")>

<cfset valueEquals(left="#lsParseDateTime( "31.12.2008",'german (standard)')#", right="{ts '2008-12-31 00:00:00'}")>


<cftry>
	<cfset valueEquals(left="#lsParseDateTime( "12/31/2008",'english (uk)')#", right="{ts '2008-12-31 00:00:00'}")>
    <cfset fail("must throw:is an invalid date or time string")>
    <cfcatch></cfcatch>
</cftry>

<cftry>
	<cfset valueEquals(left="#lsParseDateTime( "12.31.2008",'german (standard)')#", right="{ts '2008-12-31 00:00:00'}")>
    <cfset fail("must throw:is an invalid date or time string")>
    <cfcatch></cfcatch>
</cftry>

<cftry>
	<cfset valueEquals(left="#lsParseDateTime( "12/31/2008",'english (uk)')#", right="{ts '2008-12-31 00:00:00'}")>
    <cfset fail("must throw:is an invalid date or time string")>
    <cfcatch></cfcatch>
</cftry>



<cfset valueEquals(left="#lsParseDateTime( "12/30/08",'english (us)')#", right="{ts '2008-12-30 00:00:00'}")>
<cfset valueEquals(left="#lsParseDateTime( "Feb 29, 2008",'english (us)')#", right="{ts '2008-02-29 00:00:00'}")>
<cfset valueEquals(left="#lsParseDateTime( "February 29, 2008",'english (us)')#", right="{ts '2008-02-29 00:00:00'}")>
<cfset valueEquals(left="#lsParseDateTime( "Friday, February 29, 2008",'english (us)')#", right="{ts '2008-02-29 00:00:00'}")>


<cftry>
	<cfset valueEquals(left="#lsParseDateTime( "13/30/08",'english (us)')#", right="#false#")>
    <cfset fail("must throw:is an invalid date or time string")>
    <cfcatch></cfcatch>
</cftry>
<cftry>
	<cfset valueEquals(left="#lsParseDateTime( "Feb 30, 2008",'english (us)')#", right="#false#")>
    <cfset fail("must throw:is an invalid date or time string")>
    <cfcatch></cfcatch>
</cftry>
<cftry>
	<cfset valueEquals(left="#lsParseDateTime( "February 30, 2008",'english (us)')#", right="#false#")>
    <cfset fail("must throw:is an invalid date or time string")>
    <cfcatch></cfcatch>
</cftry>
<cftry>
	<cfset valueEquals(left="#lsParseDateTime( "Monday, February 29, 2008",'english (us)')#", right="#false#")>
    <cfset fail("must throw:is an invalid date or time string")>
    <cfcatch></cfcatch>
</cftry>



<cfset valueEquals(left="#lsParseDateTime( "29-Feb-2008",'english (uk)')#", right="{ts '2008-02-29 00:00:00'}")>
<cfset valueEquals(left="#lsParseDateTime( "29 February 2008",'english (uk)')#", right="{ts '2008-02-29 00:00:00'}")>
<cfset valueEquals(left="#lsParseDateTime( "Friday, 29 February 2008",'english (uk)')#", right="{ts '2008-02-29 00:00:00'}")>
<cftry>
	<cfset valueEquals(left="#lsParseDateTime( "30-Feb-2008",'english (uk)')#", right="#false#")>
    <cfset fail("must throw:is an invalid date or time string")>
    <cfcatch></cfcatch>
</cftry>
<cftry>
	<cfset valueEquals(left="#lsParseDateTime( "30 February 2008",'english (uk)')#", right="#false#")>
    <cfset fail("must throw:is an invalid date or time string")>
    <cfcatch></cfcatch>
</cftry>
<cftry>
	<cfset valueEquals(left="#lsParseDateTime( "Monday, 29 February 2008",'english (uk)')#", right="#false#")>
    <cfset fail("must throw:is an invalid date or time string")>
    <cfcatch></cfcatch>
</cftry>


<cfset valueEquals(left="#lsParseDateTime( "29. Februar 2008",'german (swiss)')#", right="{ts '2008-02-29 00:00:00'}")>
<cfset valueEquals(left="#lsParseDateTime( "Freitag, 29. Februar 2008",'german (swiss)')#", right="{ts '2008-02-29 00:00:00'}")>
<cftry>
	<cfset valueEquals(left="#lsParseDateTime( "30. Februar 2008",'german (swiss)')#", right="#false#")>
    <cfset fail("must throw:is an invalid date or time string")>
    <cfcatch></cfcatch>
</cftry>
<cftry>
	<cfset valueEquals(left="#lsParseDateTime( "Montag, 29. Februar 2008",'german (swiss)')#", right="#false#")>
    <cfset fail("must throw:is an invalid date or time string")>
    <cfcatch></cfcatch>
</cftry>
 


<cfset winter=CreateDateTime(2008,1,6,1,2,3)>
<cfset summer=CreateDateTime(2008,6,6,1,2,3)>



<cfset valueEquals(left="-#lsParseDateTime("4/6/2008","english (UK)")#", right="-{ts '2008-06-04 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("4/6/2008","english (US)")#", right="-{ts '2008-04-06 00:00:00'}")>


<cfset setlocale('German (swiss)')>
<cfset valueEquals(left="-#lsParseDateTime("01:02:03 MEZ")#", right="-{ts '1899-12-30 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02:03 MESZ")#", right="-{ts '1899-12-30 00:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02:03 GMT")#", right="-{ts '1899-12-30 02:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02:03 BST")#", right="-{ts '1899-12-30 00:02:03'}")>

<cfset valueEquals(left="-#lsParseDateTime("06.02.2008 01:02:01 MEZ")#", right="-{ts '2008-02-06 01:02:01'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.06.2008 01:02:02 MESZ")#", right="-{ts '2008-06-06 01:02:02'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.02.2008 01:02:03 MESZ")#", right="-{ts '2008-02-06 00:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.06.2008 01:02:04 MESZ")#", right="-{ts '2008-06-06 01:02:04'}")>

<cfset valueEquals(left="-#lsParseDateTime("01:02:03 MESZ")#", right="-{ts '1899-12-30 00:02:03'}")>

<cfset valueEquals(left="-#lsParseDateTime("06.04.08")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02")#", right="-{ts '1899-12-30 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02:03")#", right="-{ts '1899-12-30 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. April 2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02:03 MESZ")#", right="-{ts '1899-12-30 00:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sonntag, 6. April 2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08 01:02:03 MESZ")#", right="-{ts '2008-04-06 00:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08 1:02 Uhr MESZ")#", right="-{ts '2008-04-06 00:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08 1:02 Uhr MEZ")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08 1:02 Uhr MEZ")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.2008 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.2008 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.01.2008 01:02:03 MEZ")#", right="-{ts '2008-01-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.01.2008 1:02 Uhr MEZ")#", right="-{ts '2008-01-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.01.2008 1:02 Uhr MEZ")#", right="-{ts '2008-01-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.01.2008 01:02:03 MESZ")#", right="-{ts '2008-01-06 00:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.01.2008 1:02 Uhr MESZ")#", right="-{ts '2008-01-06 00:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.01.2008 1:02 Uhr MEZ")#", right="-{ts '2008-01-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.01.2008 1:02 Uhr MEZ")#", right="-{ts '2008-01-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.06.2008 01:02:03 MESZ")#", right="-{ts '2008-06-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.06.2008 1:02 Uhr MESZ")#", right="-{ts '2008-06-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. April 2008 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. April 2008 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. April 2008 01:02:03 MESZ")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. April 2008 1:02 Uhr MESZ")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sonntag, 6. April 2008 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sonntag, 6. April 2008 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sonntag, 6. April 2008 01:02:03 MESZ")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sonntag, 6. April 2008 1:02 Uhr MESZ")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("11:02 Uhr MEZ")#", right="-{ts '1899-12-30 11:02:00'}")>


<cfset setlocale('french (swiss)')>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02")#", right="-{ts '1899-12-30 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("6 avr. 2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02:03")#", right="-{ts '1899-12-30 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. avril 2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02:03 CEST")#", right="-{ts '1899-12-30 00:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("dimanche, 6. avril 2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01.02. h CEST")#", right="-{ts '1899-12-30 00:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08 01:02:03 CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08 01.02. h CEST")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("6 avr. 2008 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("6 avr. 2008 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("6 avr. 2008 01:02:03 CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("6 avr. 2008 01.02. h CEST")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. avril 2008 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. avril 2008 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. avril 2008 01:02:03 CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. avril 2008 01.02. h CEST")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("dimanche, 6. avril 2008 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("dimanche, 6. avril 2008 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("dimanche, 6. avril 2008 01:02:03 CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("dimanche, 6. avril 2008 01.02. h CEST")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset setlocale('italian (swiss)')>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02")#", right="-{ts '1899-12-30 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("6-apr-2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02:03")#", right="-{ts '1899-12-30 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. aprile 2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02:03 CEST")#", right="-{ts '1899-12-30 00:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("domenica, 6. aprile 2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("1.02 h CEST")#", right="-{ts '1899-12-30 00:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08 01:02:03 CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08 1.02 h CEST")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("6-apr-2008 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("6-apr-2008 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("6-apr-2008 01:02:03 CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("6-apr-2008 1.02 h CEST")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. aprile 2008 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. aprile 2008 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. aprile 2008 01:02:03 CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. aprile 2008 1.02 h CEST")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("domenica, 6. aprile 2008 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("domenica, 6. aprile 2008 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("domenica, 6. aprile 2008 01:02:03 CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("domenica, 6. aprile 2008 1.02 h CEST")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset setlocale('English (US)')>
<cfset valueEquals(left="-#lsParseDateTime("4/6/08")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("1:02 AM")#", right="-{ts '1899-12-30 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("Apr 6, 2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("1:02:03 AM")#", right="-{ts '1899-12-30 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("April 6, 2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("1:02:03 AM CEST")#", right="-{ts '1899-12-30 00:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sunday, April 6, 2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("1:02:03 AM CEST")#", right="-{ts '1899-12-30 00:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("4/6/08 1:02 AM")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("4/6/08 1:02:03 AM")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("4/6/08 1:02:03 AM CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("4/6/08 1:02:03 AM CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Apr 6, 2008 1:02 AM")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("Apr 6, 2008 1:02:03 AM")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Apr 6, 2008 1:02:03 AM CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Apr 6, 2008 1:02:03 AM CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("April 6, 2008 1:02 AM")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("April 6, 2008 1:02:03 AM")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("April 6, 2008 1:02:03 AM CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("April 6, 2008 1:02:03 AM CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sunday, April 6, 2008 1:02 AM")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sunday, April 6, 2008 1:02:03 AM")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sunday, April 6, 2008 1:02:03 AM CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sunday, April 6, 2008 1:02:03 AM CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset setlocale('English (UK)')>
<cfset valueEquals(left="-#lsParseDateTime("06/04/08")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02")#", right="-{ts '1899-12-30 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06-Apr-2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02:03")#", right="-{ts '1899-12-30 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06 April 2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02:03 CEST")#", right="-{ts '1899-12-30 00:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sunday, 6 April 2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02:03 o'clock CEST")#", right="-{ts '1899-12-30 00:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06/04/08 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06/04/08 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06/04/08 01:02:03 CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06/04/08 01:02:03 o'clock CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06-Apr-2008 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06-Apr-2008 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06-Apr-2008 01:02:03 CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06-Apr-2008 01:02:03 o'clock CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06 April 2008 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06 April 2008 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06 April 2008 01:02:03 CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06 April 2008 01:02:03 o'clock CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sunday, 6 April 2008 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sunday, 6 April 2008 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sunday, 6 April 2008 01:02:03 CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sunday, 6 April 2008 01:02:03 o'clock CEST")#", right="-{ts '2008-04-06 01:02:03'}")>


 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 

<cfset setlocale('english (us)')>
<cfset valueEquals(left="-#lsParseDateTime("01:02:03 AM CET")#", right="-{ts '1899-12-30 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02:03 AM UTC")#", right="-{ts '1899-12-30 02:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02:03 AM NST")#", right="-{ts '1899-12-30 05:32:03'}")>

<cfset setlocale('english (uk)')>
<cfset valueEquals(left="-#lsParseDateTime("06 June 2008 01:02:03 o'clock CEST","english (uk)")#", right="-{ts '2008-06-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06 February 2008 01:02:03 o'clock CEST","english (uk)")#", right="-{ts '2008-02-06 00:02:03'}")>


<cfset valueEquals(left="-#lsParseDateTime("06 June 2008 01:02:03 o'clock PDT","english (uk)")#", right="-{ts '2008-06-06 10:02:03'}")>

<cfset valueEquals(left="-#lsParseDateTime("06 April 2008 01:02:03 o'clock GMT")#", right="-{ts '2008-04-06 03:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06 April 2008 01:02:03 o'clock NST")#", right="-{ts '2008-04-06 06:32:03'}")>


<cfset setlocale('German (swiss)')>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02")#", right="-{ts '1899-12-30 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02:03")#", right="-{ts '1899-12-30 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. April 2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02:03 MESZ")#", right="-{ts '1899-12-30 00:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sonntag, 6. April 2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("1:02 Uhr MEZ")#", right="-{ts '1899-12-30 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08 01:02:03 MESZ")#", right="-{ts '2008-04-06 00:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08 1:02 Uhr MESZ")#", right="-{ts '2008-04-06 00:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.2008 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.2008 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.2008 01:02:03 MESZ")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.2008 1:02 Uhr MESZ")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. April 2008 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. April 2008 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. April 2008 01:02:03 MESZ")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. April 2008 1:02 Uhr MESZ")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sonntag, 6. April 2008 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sonntag, 6. April 2008 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sonntag, 6. April 2008 01:02:03 MESZ")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sonntag, 6. April 2008 1:02 Uhr MESZ")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset setlocale('french (swiss)')>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02")#", right="-{ts '1899-12-30 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("6 avr. 2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02:03")#", right="-{ts '1899-12-30 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. avril 2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02:03 CEST")#", right="-{ts '1899-12-30 00:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("dimanche, 6. avril 2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01.02. h CEST")#", right="-{ts '1899-12-30 00:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08 01:02:03 CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08 01.02. h CEST")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("6 avr. 2008 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("6 avr. 2008 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("6 avr. 2008 01:02:03 CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("6 avr. 2008 01.02. h CEST")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. avril 2008 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. avril 2008 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. avril 2008 01:02:03 CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. avril 2008 01.02. h CEST")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("dimanche, 6. avril 2008 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("dimanche, 6. avril 2008 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("dimanche, 6. avril 2008 01:02:03 CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("dimanche, 6. avril 2008 01.02. h CEST")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset setlocale('italian (swiss)')>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02")#", right="-{ts '1899-12-30 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("6-apr-2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02:03")#", right="-{ts '1899-12-30 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. aprile 2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02:03 CEST")#", right="-{ts '1899-12-30 00:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("domenica, 6. aprile 2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("1.02 h CEST")#", right="-{ts '1899-12-30 00:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08 01:02:03 CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.04.08 1.02 h CEST")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("6-apr-2008 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("6-apr-2008 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("6-apr-2008 01:02:03 CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("6-apr-2008 1.02 h CEST")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. aprile 2008 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. aprile 2008 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. aprile 2008 01:02:03 CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("6. aprile 2008 1.02 h CEST")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("domenica, 6. aprile 2008 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("domenica, 6. aprile 2008 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("domenica, 6. aprile 2008 01:02:03 CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("domenica, 6. aprile 2008 1.02 h CEST")#", right="-{ts '2008-04-06 01:02:00'}")>

<cfset setlocale('English (US)')>
<cfset valueEquals(left="-#lsParseDateTime("4/6/08")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("1:02 AM")#", right="-{ts '1899-12-30 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("Apr 6, 2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("1:02:03 AM")#", right="-{ts '1899-12-30 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("April 6, 2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("1:02:03 AM CEST")#", right="-{ts '1899-12-30 00:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sunday, April 6, 2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("1:02:03 AM CEST")#", right="-{ts '1899-12-30 00:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("4/6/08 1:02 AM")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("4/6/08 1:02:03 AM")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("4/6/08 1:02:03 AM CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("4/6/08 1:02:03 AM CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Apr 6, 2008 1:02 AM")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("Apr 6, 2008 1:02:03 AM")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Apr 6, 2008 1:02:03 AM CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Apr 6, 2008 1:02:03 AM CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("April 6, 2008 1:02 AM")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("April 6, 2008 1:02:03 AM")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("April 6, 2008 1:02:03 AM CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("April 6, 2008 1:02:03 AM CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sunday, April 6, 2008 1:02 AM")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sunday, April 6, 2008 1:02:03 AM")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sunday, April 6, 2008 1:02:03 AM CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sunday, April 6, 2008 1:02:03 AM CEST")#", right="-{ts '2008-04-06 01:02:03'}")>



<cfset setlocale('English (UK)')>
<cfset valueEquals(left="-#lsParseDateTime("06/04/08 01:02:03 GMT")#", right="-{ts '2008-04-06 03:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06/04/08 01:02:03 CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06/04/08 01:02:03 NST")#", right="-{ts '2008-04-06 06:32:03'}")>


<cfset valueEquals(left="-#lsParseDateTime("06/04/08")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02")#", right="-{ts '1899-12-30 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06-Apr-2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02:03")#", right="-{ts '1899-12-30 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06 April 2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02:03 CEST")#", right="-{ts '1899-12-30 00:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sunday, 6 April 2008")#", right="-{ts '2008-04-06 00:00:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("01:02:03 o'clock CEST")#", right="-{ts '1899-12-30 00:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06/04/08 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06/04/08 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06/04/08 01:02:03 CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06/04/08 01:02:03 o'clock CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06-Apr-2008 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06-Apr-2008 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06-Apr-2008 01:02:03 CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06-Apr-2008 01:02:03 o'clock CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06 April 2008 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("06 April 2008 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06 April 2008 01:02:03 CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06 April 2008 01:02:03 o'clock CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06 April 2008 01:02:03 o'clock GMT")#", right="-{ts '2008-04-06 03:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06 April 2008 01:02:03 o'clock NST")#", right="-{ts '2008-04-06 06:32:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sunday, 6 April 2008 01:02")#", right="-{ts '2008-04-06 01:02:00'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sunday, 6 April 2008 01:02:03")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sunday, 6 April 2008 01:02:03 CEST")#", right="-{ts '2008-04-06 01:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("Sunday, 6 April 2008 01:02:03 o'clock CEST")#", right="-{ts '2008-04-06 01:02:03'}")>

<cfset setLocale("German (Swiss)")>
<cfset dt=CreateDateTime(2004,1,2,4,5,6)>

<!--- @todo do also time not only date
<cfset valueEquals(left="#LSParseDateTime("02.01.2004")#x", right="{ts '2004-01-02 00:00:00'}x")>
<cfset valueEquals(left="#LSParseDateTime("02.01.04")#x", right="{ts '2004-01-02 00:00:00'}x")>
<cfset valueEquals(left="#LSParseDateTime("2. Januar 2004")#x", right="{ts '2004-01-02 00:00:00'}x")>
<cfset valueEquals(left="#LSParseDateTime("Freitag, 2. Januar 2004")#x", right="{ts '2004-01-02 00:00:00'}x")></strong>
 --->
 <!---
<cfset setlocale('english (uk)')>
<cfset valueEquals(left="-#lsParseDateTime("06.02.2008 01:02:01")#",  right="-{ts '2008-02-06 01:02:01'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.06.2008 01:02:02")#", right="-{ts '2008-06-06 01:02:02'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.02.2008 01:02:03")#", right="-{ts '2008-02-06 00:02:03'}")>
<cfset valueEquals(left="-#lsParseDateTime("06.06.2008 01:02:04")#", right="-{ts '2008-06-06 01:02:04'}")>
--->


<cfset valueEquals(left="-#LSParseDateTime("1/1",'en_us')#", right="-{ts '#year(now())#-01-01 00:00:00'}")>
<cfoutput>
<cfloop list="ar_SA,zh_CN,zh_TW,nl_NL,en_AU,en_CA,en_GB,fr_CA,fr_FR,de_DE,iw_IL,hi_IN,it_IT,ja_JP,ko_KR,pt_BR,es_ES,sv_SE,th_TH,th_TH_TH" index="locale">
<cftry>
	<cfset LSParseDateTime("1/1",locale)>
    <cfset fail("must throw:#locale# -)> can't cast [1/1] to date value")>
    <cfcatch></cfcatch>
</cftry>
</cfloop>
</cfoutput> 

<cfset setLocale('english (australian)')>
<cfset valueEquals(left="#LSParseDateTime('01/02/2010')#x", right="{ts '2010-02-01 00:00:00'}x")>
<cfset setLocale(orgLocale)>


<cfset str="6014.10">
<cfset valueEquals(left="#isDate(str)#", right="true")>
<cfset valueEquals(left="#parseDateTime(str)#", right="{ts '6014-10-01 00:00:00'}")>

<cftry>
	<cfset lsparseDateTime(str)>
	must throw a error!
	<cfcatch></cfcatch>
</cftry>


<!--- format --->

<!--- not supported in CFML <= 9  --->
<cfif listFirst(server.ColdFusion.ProductVersion,',') GT 9> 
    <cfset valueEquals(left="-#lsParseDateTime("1/30/02 7:02:33",'en','m/dd/yy h:mm:ss')#", right="-{ts '2002-01-30 07:02:33'}")>
    <cfset valueEquals(left="-#lsParseDateTime("1/30/02 7:02:33",'en','m/dd/yy h:mm:ss')#", right="-{ts '2002-01-30 07:02:33'}")>
    <cfset valueEquals(left="-#lsParseDateTime("1/30/2002 7:02 AM",'en','m/dd/yyyy h:mm')#", right="-{ts '2002-01-30 07:02:00'}")>
</cfif>

<!--- <cfset valueEquals(left="-#lsParseDateTime("Wednesday, January 30, 2002 7:02:12 AM PST",'en','dddd, mmmm dd, yyyy h:mm:ss a z')#", right="-")>



<cfset valueEquals(left="-#lsParseDateTime("Wed, Jan 30, 2002 07:02:12",'en','ddd, mmm dd, yyyy hh:mm:ss')#", right="-")>
<cfset valueEquals(left="-#lsParseDateTime("January 30, 2002 7:02:23 AM PST",'en','mmmm dd, yyyy h:mm:ss tt zzz')#", right="-")>
<cfset valueEquals(left="-#lsParseDateTime("Jan 30, 2002 7:02:12 AM",'en','mmm dd, yyyy h:mm:ss tt')#", right="-")>
<cfset valueEquals(left="-#lsParseDateTime("1/30/02 7:02 AM",'en','m/dd/yy h:mm tt')#", right="-")>--->

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>