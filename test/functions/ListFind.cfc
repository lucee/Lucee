<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<!---
	<cffunction name="beforeTests"></cffunction>
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->
	<cffunction name="testListFind" localMode="modern">

<!--- begin old test code --->
<cfset valueEquals(left="#ListFind('abba,bb','bb')#", right="2")>
<cfset valueEquals(left="#ListFind('abba,bb,AABBCC,BB','BB')#", right="4")>
<cfset valueEquals(left="#ListFind('abba,bb,AABBCC','ZZ')#", right="0")>
<cfset valueEquals(left="#ListFind(',,,,abba,bb,AABBCC','bb')#", right="2")>
<cfset valueEquals(left="#ListFind(',,,,abba,,,,bb,AABBCC','bb')#", right="2")>
<cfset valueEquals(left="#ListFind(',,,,abba,,,,bb,AABBCC','bb','.,;')#", right="2")>




<cfset valueEquals(left="#ListFind('a,,c','c',',',false)#", right="2")>
<cfset valueEquals(left="#ListFind('a,,c','c',',',true)#", right="3")>

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
