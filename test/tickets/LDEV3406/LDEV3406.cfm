<cfparam name="FORM.colName" default="">

<cfquery name="test">
    select #form.colName# from LDEV3406
</cfquery>
<cfoutput>#serializeJSON(test)#</cfoutput>