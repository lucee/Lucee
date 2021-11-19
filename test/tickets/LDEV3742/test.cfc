<cfcomponent>
	<cffunction name="withType" access="remote">
		<cfcontent type="image/png" file="#expandPath('image.png')#">
	</cffunction>

	<cffunction name="withoutType" access="remote">
		<cfcontent file="#expandPath('image.png')#">
	</cffunction>
</cfcomponent>
