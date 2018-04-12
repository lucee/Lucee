<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<!---
	<cffunction name="beforeTests"></cffunction>
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->
	<cffunction name="testListFindNoCase" localMode="modern">

<!--- begin old test code --->
<cfset valueEquals(left="#ListFindNoCase('abba,bb','bb')#", right="2")>
<cfset valueEquals(left="#ListFindNoCase('abba,bb,AABBCC,BB','BB')#", right="2")>
<cfset valueEquals(left="#ListFindNoCase('abba,bb,AABBCC','ZZ')#", right="0")>
<cfset valueEquals(left="#ListFindNoCase('abba,,,,,,,bb,AABBCC,BB','BB')#", right="2")>
<cfset valueEquals(left="#ListFindNoCase(',,,abba,,,,,,,bb,AABBCC,BB','BB')#", right="2")>
<cfset valueEquals(left="#ListFindNoCase(',,,abba,,,,,,,bb,AABBCC,BB','BB','.,;')#", right="2")>


<cfset valueEquals(left="#ListFindNoCase('a,,c','c',',',false)#", right="2")>
<cfset valueEquals(left="#ListFindNoCase('a,,c','c',',',true)#", right="3")>
<cfset valueEquals(left="#ListFindNoCase('a,,c','C',',',false)#", right="2")>

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
