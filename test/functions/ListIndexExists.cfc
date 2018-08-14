<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<!---
	<cffunction name="beforeTests"></cffunction>
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->
	<cffunction name="testListIndexExists" localMode="modern">

<!--- begin old test code --->
<cfif server.ColdFusion.ProductName EQ "railo">
	<cfset valueEquals(left="#ListIndexExists("a,b,c,d",2)#", right="yes")>
	<cfset valueEquals(left="#ListIndexExists("a,b,c,d",4)#", right="yes")>
	<cfset valueEquals(left="#ListIndexExists("a,b,c,d",5)#", right="no")>
	<cfset valueEquals(left="#ListIndexExists("a,b,c,d",5,'.,;')#", right="no")>
    
    
    
    
	<cfset valueEquals(left="#ListIndexExists(",,,,,,a,b,c,d",5,'.,;')#", right="no")>
	<cfset valueEquals(left="#ListIndexExists(",,,,,,a,b,c,d",5,'.,;',false)#", right="no")>
	<cfset valueEquals(left="#ListIndexExists(",,,,,,a,b,c,d",5,'.,;',true)#", right="yes")>
    
    
</cfif>

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
