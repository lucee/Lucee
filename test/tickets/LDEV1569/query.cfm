<cfquery name="test" returntype="array">
    select id, name from test where id=1
</cfquery>
<cfscript>
    echo(SerializeJSON(test));
</cfscript>