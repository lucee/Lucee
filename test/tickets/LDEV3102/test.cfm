<cfparam name="FORM.scene" default="">
<cfparam name="FORM.insert" default="false">
<cfif FORM.scene eq 1 >
	<cftry>	
		<cfquery name="qryGetData" datasource="ldev3102_DSN" result="qryGetResult" >
			<cfif FORM.insert>
				insert into ldev3102 values (2,'inserted')
			</cfif>
			select * from ldev3102
		</cfquery>
		<cfoutput>#qryGetData.recordcount#</cfoutput>
		<cfcatch>
			<cfoutput>#cfcatch.message#</cfoutput>
		</cfcatch>
	</cftry>
<cfelseif FORM.scene eq 2>
	<cfquery name="qryGetData" datasource="ldev3102_DSN" >
		insert into ldev3102 values (2,'inserted')
		select * from ldev3102
	</cfquery>
	<cfoutput>#qryGetData.recordcount#</cfoutput>
<cfelse>
	<cfquery name="qryGetData" datasource="ldev3102_DSN" result="qryGetResult">
		select * from ldev3102
		insert into ldev3102 values (2,'inserted')
	</cfquery>
	<cfoutput>#qryGetData.recordcount#</cfoutput>
</cfif>