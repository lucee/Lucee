<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<!---
	<cffunction name="beforeTests"></cffunction>
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->
	<cffunction name="testLSEuroCurrencyFormat" localMode="modern">

<!--- begin old test code --->
<cfset orgLocale=getLocale()>
<cfset setLocale("English (UK)")>
<cfset dt=CreateDateTime(2004,1,2,4,5,6)>
<cfset euro=chr(8364)>
<cfset pound=chr(163)>

<!--- changed from german swiss locale to UK locale because the JVM chnaged the symbol used for swiss between Java 8 and 10 --->

<cfset valueEquals(left="#LSEuroCurrencyFormat(1)#", right="#pound#1.00")>
<cfset valueEquals(left="#LSEuroCurrencyFormat(1.2)#", right="#pound#1.20")>

<cfset valueEquals(left="#LSEuroCurrencyFormat(1.2,"local")#", right="#pound#1.20")>
<cfset valueEquals(left="#replace(LSEuroCurrencyFormat(1.2,"international","English (UK)")," ","")#", right="GBP1.20")>
<cfset valueEquals(left="#LSEuroCurrencyFormat(1.2,"none")#", right="1.20")>



<cftry>
	<cfset valueEquals(left="#LSEuroCurrencyFormat(1.2,"susi")#", right="x")>
	<cfset fail("must throw:Parameter 2 of function LSCurrencyFormat has an invalid value of ""susi"". "".""."".""."".")>
	<cfcatch></cfcatch>
</cftry>
 

<cfset setLocale("German (Standard)")>
<cfset valueEquals(left=ascs(LSEuroCurrencyFormat(1)), right=ascs("1,00 #euro#"))>
<cfset valueEquals(left=LSEuroCurrencyFormat(1), right="1,00 #euro#")>
<cfset valueEquals(left=LSEuroCurrencyFormat(1.2), right="1,20 #euro#")>

<cfset valueEquals(left="#LSEuroCurrencyFormat(1.2,"local")#", right="1,20 #euro#")>
<cfset valueEquals(left=ascs(LSEuroCurrencyFormat(1.2,"international")), right=ascs("EUR 1,20"))	>
<cfset valueEquals(left="#LSEuroCurrencyFormat(1.2,"international")#", right="EUR 1,20")>
<cfset valueEquals(left="#LSEuroCurrencyFormat(1.2,"none")#", right="1,20")>
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
private function ascs(str) {
	var l=len(str);
	var rtn="";
	for(var i=1;i<=l;i++) {
	    rtn=listAppend(rtn,asc(str[i]),'-');
	}
    return "["&str&"] -> "&rtn;
}
</cfscript>
	
</cfcomponent>