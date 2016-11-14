<cfoutput>
<cftry>
	<cfset imgfile = "../../artifacts/image.jpg" />
	<cfimage source="#imgfile#" name="myImage" />
	<cfset imageGrayscale(myImage) />
	<cfimage action="WRITE" source="#myImage#" destination="#imgfile#_grayscale.jpg" overwrite="yes" />
	done
	<cfcatch type="any">
		#cfcatch.Message#
	</cfcatch>
</cftry>
</cfoutput>