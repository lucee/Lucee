<!--- empty on purpose --->

<cfset start=getTickCount()>
<cfquery name="qry">
select 'ok' as Foobar
</cfquery>
<cfoutput>:#qry.foobar#:#getTickCount()-start#:</cfoutput>