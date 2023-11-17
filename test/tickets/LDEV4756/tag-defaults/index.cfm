<cfif url.tagDefaults ?: false>
	<cfcookie name="VALUE" value="LDEV4756" partitioned="#partitioned#" path="#url.path#" secure="#url.secure#">
<cfelse>
	<cfcookie name="VALUE" value="LDEV4756">
</cfif>
