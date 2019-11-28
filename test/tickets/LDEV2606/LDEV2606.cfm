<cfparam name="form.scene" default="1">
<cfif form.scene eq 1>
	<cfset form.id = 1>
	<cfset form.value = 'on'>
	<cfupdate datasource="LDEV2606_DSN" tablename="LDEV2606" formfields="id,value">
	<cfquery name="update" datasource="LDEV2606_DSN">
		select * from LDEV2606
	</cfquery>
	<cfoutput>#update.value[1]#</cfoutput>

<cfelseif form.scene eq 2>
	<cfset form.id = 2>
	<cfset form.value = 1>
	<cfupdate datasource="LDEV2606_DSN" tablename="LDEV2606" formfields="id,value">
	<cfquery name="update" datasource="LDEV2606_DSN">
		select * from LDEV2606
	</cfquery>
	<cfoutput>#update.value[2]#</cfoutput>
</cfif>