<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<!---
	<cffunction name="beforeTests"></cffunction>
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->
	<cffunction name="testLSCurrencyFormat" localMode="modern">

<!--- begin old test code --->
<cfset orgLocale=getLocale()>

<cfset dt=CreateDateTime(2004,1,2,4,5,6)>
<cfset euro=chr(8364)>
<!--- 
English (Australian) --->
<cfset setLocale("English (Australian)")>
<cfset valueEquals(left="#LSCurrencyFormat(100000)#", right="$100,000.00")>
<cfset valueEquals(left="#LSCurrencyFormat(100000,"none")#", right="100,000.00")>
<cfset valueEquals(left="#LSCurrencyFormat(100000,"local")#", right="$100,000.00")>
<cfset valueEquals(left="#replace(LSCurrencyFormat(100000,"international","English (Australian)"),' ','')#", right="AUD100,000.00")>

<!--- 
German (Standard)) --->
<cfset setLocale("German (Standard)")>
<cfset valueEquals(left="#asc(right(LSCurrencyFormat(100000,"local"),1))#", right="#asc(euro)#")>
<cfset valueEquals(left="#trim(LSCurrencyFormat(100000,"local"))#", right="100.000,00 #euro#")>
<cfset valueEquals(left="#LSCurrencyFormat(100000,"international")#", right="EUR 100.000,00")>
<cfset valueEquals(left="#LSCurrencyFormat(100000,"none")#", right="100.000,00")>

<cftry>
	<cfset valueEquals(left="#LSCurrencyFormat(1.2,"susi")#", right="x")>
	<cfset fail("must throw:Parameter 2 of function LSCurrencyFormat has an invalid value of ""susi"". "".""."".""."".""."".""."".""."".")>
	<cfcatch></cfcatch>
</cftry>


<!--- 
German (Standard) --->
<cfset setLocale("German (Standard)")>

<cfset valueEquals(left="#LSCurrencyFormat(1)#", right="1,00 #euro#")>
<cfset valueEquals(left="#LSCurrencyFormat(1.2)#", right="1,20 #euro#")>

<cfset valueEquals(left="#LSCurrencyFormat(1.2,"local")#", right="1,20 #euro#")>
<cfset valueEquals(left="#replace(LSCurrencyFormat(1.2,"international")," ","")#", right="EUR1,20")>
<cfset valueEquals(left="#LSCurrencyFormat(1.2,"none")#", right="1,20")>

<cfset setLocale("Portuguese (Brazilian)")>

<cfset value=250000>
<cfset valueEquals(left="#LSParseNumber(value)#", right="250000")>
<cfset valueEquals(left="#replace(LSCurrencyFormat(value),' ','')#", right="R$250.000,00")>

<cfset value=250.000>
<cfset valueEquals(left="#LSParseNumber(value)#", right="250")>
<cfset valueEquals(left="#replace(LSCurrencyFormat(value),' ','')#", right="R$250,00")>

<cfset value="250000">
<cfset valueEquals(left="#LSParseNumber(value)#", right="250000")>
<cfset valueEquals(left="#replace(LSCurrencyFormat(value),' ','')#", right="R$250.000,00")>

<cfset value="250,000">
<cfset valueEquals(left="#LSParseNumber(value)#", right="250")>


<cfset value="250.000">
<cfset valueEquals(left="#LSParseNumber(value)#", right="250000")>
<cfset valueEquals(left="#replace(LSCurrencyFormat(value,"local","Portuguese (Brazilian)"),' ','','all')#", right="R$250,00")>
<cfset valueEquals(left="#replace(LSCurrencyFormat(value,'international'),' ','','all')#", right="BRL250,00")>
<cfset valueEquals(left="#LSCurrencyFormat(value,'none')#", right="250,00")>
<cfset valueEquals(left="#replace(LSCurrencyFormat(value),' ','','all')#", right="R$250,00")>

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