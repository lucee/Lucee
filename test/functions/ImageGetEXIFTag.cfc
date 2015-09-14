<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<!---
	<cffunction name="beforeTests"></cffunction>
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->
	<cffunction name="testImageGetEXIFTag" localMode="modern">

<!--- begin old test code --->
<cfset img=imageRead(GetDirectoryFromPath(GetCurrentTemplatePath())&"images/BigBen.jpg")>
<cfset valueEquals(left="#ImageGetEXIFTag(img,'Subject Location')#", right="1631 1223 1795 1077")>
<cfset valueEquals(left="#ImageGetEXIFTag(img,'Thumbnail Compression')#", right="JPEG (old-style)")>

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
