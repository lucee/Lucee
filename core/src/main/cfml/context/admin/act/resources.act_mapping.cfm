<cfparam name="url.job" default="" type="string">
<cfparam name="url.mapping_id" default="-1" type="numeric">
<cfset mappings=getPageContext().getConfig().getMappings()>
<cfif url.job eq "save" AND url.mapping_id gt -1>
	<cfset mappings=getPageContext().getConfig().getMappings()>
	<cfset mappings[url.mapping_id].physical = form.physical>
	<cfset mappings[url.mapping_id].virtual  = form.virtual>
	<cfset mappings[url.mapping_id].archive  = form.archive>
	<cfif isDefined("form.trusted")>
		<cfset mappings[url.mapping_id].trusted  = True>
	<cfelse>
		<cfset mappings[url.mapping_id].trusted  = False>
	</cfif>
</cfif>
<cfif url.job eq "#stText.Buttons.Delete#">
	<cfset tmp = ArrayDeleteAt(mappings, url.mapping_id)>
<cfelseif url.job eq "add">
	<cfset tmp = ArrayAppend(mappings, StructNew())>
	<cfset url.mapping_id = ArrayLen(mappings)>
	<cfset mappings[url.mapping_id].physical = form.physical>
	<cfset mappings[url.mapping_id].virtual  = form.virtual>
	<cfset mappings[url.mapping_id].archive  = form.archive>
	<cfif isDefined("form.trusted")>
		<cfset mappings[url.mapping_id].trusted  = True>
	<cfelse>
		<cfset mappings[url.mapping_id].trusted  = False>
	</cfif>
</cfif> 