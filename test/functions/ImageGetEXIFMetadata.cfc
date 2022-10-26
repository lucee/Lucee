<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">

	<cffunction name="testImageGetEXIFMetadata" localMode="modern">
		<cfset img=imageRead(GetDirectoryFromPath(GetCurrentTemplatePath())&"images/BigBen.jpg")>
		<cfset assertEquals("1",ImageGetEXIFTag(img,'ColorSpace'))>
		<cfset assertEquals("204",ImageGetEXIFTag(img,'ExifOffset'))>
	</cffunction>
</cfcomponent>
