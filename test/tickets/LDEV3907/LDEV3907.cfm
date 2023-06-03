<cftry>
	<cfparam name="url.pk" default="true">
	<cfset brand = new Brand() />
	<cfif url.pk>
		<cfset brand.setBrandID("") />
	</cfif>
	<cfset brand.setBrandName("LDEV3907") />
	<cfset entitySave(brand) />
	<cfoutput>#brand.getBrandName()#</cfoutput>
	<cfset ormFlush() />
	<cfcatch>
		<cfoutput>#cfcatch.stacktrace#</cfoutput>
	</cfcatch>
</cftry>