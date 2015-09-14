<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<!---
	<cffunction name="beforeTests"></cffunction>
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->
	<cffunction name="testImageGetEXIFMetadata" localMode="modern">
		
<!--- begin old test code --->
		<cfset img=imageRead(GetDirectoryFromPath(GetCurrentTemplatePath())&"images/BigBen.jpg")>
		<cfset meta=ImageGetEXIFMetadata(img)>

		<cfset keys="Subject Location,Thumbnail Compression,White Balance Mode">
		<cfloop list="#keys#" index="key">
			<cfset meta[key]>
		</cfloop>
		<cfset valueEquals(left="#ImageGetEXIFTag(img,'Color Space')#", right="sRGB")>

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
