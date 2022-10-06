<cfif thisTag.executionMode is 'start'>
	<cfparam name='attributes.value' type='string' default='0'>
	<cfoutput>#DollarFormat(val(attributes.value))#</cfoutput>
</cfif> 