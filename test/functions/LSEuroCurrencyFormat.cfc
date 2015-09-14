<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<!---
	<cffunction name="beforeTests"></cffunction>
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->
	<cffunction name="testLSEuroCurrencyFormat" localMode="modern">

<!--- begin old test code --->
<cfset orgLocale=getLocale()>
<cfset setLocale("German (Swiss)")>
<cfset dt=CreateDateTime(2004,1,2,4,5,6)>
<cfset euro=chr(8364)>






<cfset valueEquals(left="#LSEuroCurrencyFormat(1)#", right="SFr. 1.00")>
<cfset valueEquals(left="#LSEuroCurrencyFormat(1.2)#", right="SFr. 1.20")>

<cfset valueEquals(left="#LSEuroCurrencyFormat(1.2,"local")#", right="SFr. 1.20")>
<cfset valueEquals(left="#replace(LSEuroCurrencyFormat(1.2,"international","German (Swiss)")," ","")#", right="CHF1.20")>
<cfset valueEquals(left="#LSEuroCurrencyFormat(1.2,"none")#", right="1.20")>



<cftry>
	<cfset valueEquals(left="#LSEuroCurrencyFormat(1.2,"susi")#", right="x")>
	<cfset fail("must throw:Parameter 2 of function LSCurrencyFormat has an invalid value of ""susi"". "".""."".""."".")>
	<cfcatch></cfcatch>
</cftry>
 

<cfset setLocale("German (Standard)")>
<cfset valueEquals(left="#LSEuroCurrencyFormat(1)#", right="1,00 #euro#")>
<cfset valueEquals(left="#LSEuroCurrencyFormat(1.2)#", right="1,20 #euro#")>

<cfset valueEquals(left="#LSEuroCurrencyFormat(1.2,"local")#", right="1,20 #euro#")>
<cfset valueEquals(left="#replace(LSEuroCurrencyFormat(1.2,"international")," ","")#", right="EUR1,20")>
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
</cfcomponent>