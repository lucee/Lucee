<cfparam name = "Form.scene" default = "">
<cfscript>
    _test = queryNew("_id,_name,_cmpnyName","integer,varchar,varchar", [[01,'saravana', 'MitrahSoft'],[07, 'pothys','MitrahSoft'], [09, 'MichaelOffener','RASIA']]);
</cfscript>
<cfoutput>
    <cfif form.scene eq 1>
        <cftry>
            <cfset hasError = false>
            <cfquery name="qTest" dbtype="query">
                SELECT * FROM _test
                WHERE _id = <cfqueryparam cfsqltype="cf_sql_integer" value="" null="false" /> 
            </cfquery>
            <cfcatch>
                <cfset hasError = true>
            </cfcatch>
        </cftry>
        #hasError#
    <cfelseif form.scene eq 2>
        <cfquery name="qTest" dbtype="query">
            SELECT * FROM _test
            WHERE _id = <cfqueryparam cfsqltype="cf_sql_integer" value="" null="true" /> 
        </cfquery>
        #qTest.recordcount#
    <cfelseif form.scene eq 3>
        <cfquery name="qTest" dbtype="query">
            SELECT * FROM _test
            WHERE _id = <cfqueryparam cfsqltype="cf_sql_integer" value="01" null="false" /> 
        </cfquery>
        #qTest.recordcount#
    <cfelseif form.scene eq 4>
        <cfquery name="qTest" dbtype="query">
            SELECT * FROM _test
            WHERE _id = <cfqueryparam cfsqltype="cf_sql_integer" value="01" null="true" /> 
        </cfquery>
        #qTest.recordcount#
    <cfelseif form.scene eq 5>
        <cftry>
            <cfset hasError = false>
            <cfquery name="qTest">
                SELECT * FROM LDEV1532
                WHERE id = <cfqueryparam cfsqltype="cf_sql_integer" value="" null="false" /> 
            </cfquery>
            <cfcatch>
                <cfset hasError = true>
            </cfcatch>
        </cftry>
        #hasError#
    <cfelseif form.scene eq 6>
        <cfquery name="qTest">
            SELECT * FROM LDEV1532
        WHERE id = <cfqueryparam cfsqltype="cf_sql_integer" value="" null="true" /> 
        </cfquery>
        #qTest.recordcount#
    <cfelseif form.scene eq 7>
        <cfquery name="qTest">
            SELECT * FROM LDEV1532
        WHERE id = <cfqueryparam cfsqltype="cf_sql_integer" value="1" null="false" /> 
        </cfquery>
        #qTest.recordcount#
    <cfelseif form.scene eq 8>
        <cfquery name="qTest">
            SELECT * FROM LDEV1532
        WHERE id = <cfqueryparam cfsqltype="cf_sql_integer" value="1" null="true" /> 
        </cfquery>
        #qTest.recordcount#
    </cfif> 
</cfoutput>