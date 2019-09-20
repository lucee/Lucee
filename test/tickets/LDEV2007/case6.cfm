<cfset myQuery = queryNew("name,age,dept","varchar,integer,varchar", [['aaa',35,'CEO'],['bbb',20, 'Employee']])>
<cfloop query="#myQuery#" item="i">
</cfloop>