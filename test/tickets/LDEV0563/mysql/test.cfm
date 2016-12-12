<cfparam name="FORM.Scene" default="1">
<cfset error = "">
<cfquery datasource="DSN1">
	DELETE FROM users1;
</cfquery>
<cfquery datasource="DSN2">
	DELETE FROM users2;
</cfquery>
<cfquery datasource="DSN3">
	DELETE FROM users3;
</cfquery>

<cfquery datasource="DSN1">
	INSERT INTO users1 VALUES('POTHYS1')
</cfquery>
<cfquery datasource="DSN2">
	INSERT INTO users2 VALUES('POTHYS2')
</cfquery>
<cfquery datasource="DSN3">
	INSERT INTO users3 VALUES('POTHYS3')
</cfquery>

<cftry>
	<cftransaction>
		<cfquery datasource="DSN1">
			INSERT INTO users1 VALUES('SARAVANA1')
		</cfquery>
		<cfquery datasource="DSN2">
			INSERT INTO users2 VALUES('SARAVANA2')
		</cfquery>
		<cfif FORM.Scene EQ 1>
			<cfquery datasource="DSN3">
				INSERT INTO users3 VALUES('SARAVANA3')
			</cfquery>
		<cfelse>
			<cfquery datasource="DSN3">
				INSERT INTO users4 VALUES('SARAVANA4')
			</cfquery>
		</cfif>
	</cftransaction>
	<cfcatch type="any">
		<cfif len(cfcatch.Detail)>
			<cfset error = cfcatch.Detail>
		<cfelse>
			<cfset error = cfcatch.Message>
		</cfif>
	</cfcatch>
</cftry>

<cfquery name="myQuery1" datasource="DSN1">
	SELECT * FROM users1
</cfquery>
<cfquery name="myQuery2" datasource="DSN2">
	SELECT * FROM users2
</cfquery>
<cfquery name="myQuery3" datasource="DSN3">
	SELECT * FROM users3
</cfquery>
<cfoutput>#error#|#myQuery1.RecordCount#|#myQuery2.RecordCount#|#myQuery3.RecordCount#</cfoutput>