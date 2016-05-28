<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<!---
	<cffunction name="beforeTests"></cffunction>
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->

	<cffunction name="testURLDecodeMember" localMode="modern">
		<cfset valueEquals(left="#"+".URLDecode()#", right=" ")>
	
	</cffunction>

	<cffunction name="testURLDecode" localMode="modern">

<!--- begin old test code --->
<cfset valueEquals(left="#URLDecode("123")#", right="123")>
<cfset valueEquals(left="#URLDecode("+")#", right=" ")>
<cfset valueEquals(left="#URLDecode("%20")#", right=" ")>


<cfset valueEquals(left="#URLEncodedFormat('%')#", right="%25")>
<cfset valueEquals(left="#URLDecode(URLEncodedFormat('%'))#", right="%")>


<cfset valueEquals(left="#URLEncodedFormat('%&/')#", right="%25%26%2F")>
<cfset valueEquals(left="#URLDecode('%&/')#", right="%&/")>
<cfset valueEquals(left="#URLDecode('%')#", right="%")>

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
