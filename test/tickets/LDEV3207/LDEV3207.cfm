<cfparam name="form.scene" defaultvalue="1">
<cfif form.scene eq 1>
    <cftry>
        <cfquery datasource="LDEV3207" timeout="1" name="q">
            select * from value_table
        </cfquery>
        <cfquery datasource="LDEV3207" timeout="1" name="res">
            select * from value_table
            WHERE
            value IN ( <cfqueryparam cfsqltype="cf_sql_varchar" value="#valuelist(q.value)#" list="true" null="true"> )
        </cfquery>
       <cfoutput>success</cfoutput>
       <cfcatch>
           <cfoutput>#cfcatch.message#</cfoutput>
       </cfcatch>
    </cftry>
<cfelse>
    <cftry>
        <cfquery datasource="LDEV3207" timeout="1" name="q">
            select * from empty_table
        </cfquery>
        <cfquery datasource="LDEV3207" timeout="1" name="res">
            select * from empty_table
            WHERE
            value IN ( <cfqueryparam cfsqltype="cf_sql_varchar" value="#valuelist(q.value)#" list="true" null="true"> )
        </cfquery>
        <cfoutput>success</cfoutput>
        <cfcatch>
            <cfoutput>#cfcatch.message#</cfoutput>
        </cfcatch>
    </cftry>
</cfif>