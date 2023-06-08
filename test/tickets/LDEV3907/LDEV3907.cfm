<cftry>
	<cfparam name="url.pk" default="true">
	<cfparam name="url.force" default="false">
	
	<cfset brand = new Brand() />
	<cfif url.pk>
		<cfset brand.setBrandID("") />
	</cfif>
	<cfset brand.setBrandName("LDEV3907") />
	<cfset entitySave(brand, url.force) />
	<cfoutput>#brand.getBrandName()#</cfoutput>
	<cfset ormFlush() />
	<cfcatch>
		<cfoutput>#cfcatch.stacktrace#</cfoutput>
	</cfcatch>
</cftry>