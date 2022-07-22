<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase" labels="image">

	<cffunction name="testImageGetEXIFMetadata" localMode="modern">
		
		<cfset img=imageRead(GetDirectoryFromPath(GetCurrentTemplatePath())&"images/BigBen.jpg")>
		<cfset meta=ImageGetEXIFMetadata(img)>
		<cfset keys="Subject Location,Thumbnail Compression,White Balance Mode">
		<cfloop list="#keys#" index="key">
			<cfset meta[key]>
		</cfloop>
		<cfset assertEquals("1",ImageGetEXIFTag(img,'ColorSpace'))>
		<cfset assertEquals("204",ImageGetEXIFTag(img,'ExifOffset'))>
	</cffunction>
</cfcomponent>
