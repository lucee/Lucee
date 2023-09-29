<cfoutput>
<cftry>
	<cfset imgfile = "../../artifacts/image.jpg" />
	<cfimage source="#imgfile#" name="myImage" />
	<cfset imageGrayscale(myImage) />
	<cfset dest = getTempFile(getTempDirectory(), "LDEV0595", "jpg")>
	<cfimage action="WRITE" source="#myImage#" destination="#dest#" overwrite="yes" />
	done
	<cfcatch type="any">
		#cfcatch.Message#
	</cfcatch>
</cftry>
</cfoutput>