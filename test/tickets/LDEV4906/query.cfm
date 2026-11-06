<cfset qs = queryNew( "id", "integer", [ [ 1 ] ] )>
<cfquery name="q1" dbtype="query">
	select id from qs where 'c' in ( #listQualify( "a b, c", "'", ", ", "all", false, true )# )
</cfquery>
<cfquery name="q2" dbtype="query">
	select id from qs where 'a' in ( #listQualify( "a b, c", "'", ", ", "all", false, true )# )
</cfquery>
<cfquery name="q3" dbtype="query">
	select id from qs where 'it''s' in ( #listQualify( "it's, c", "'", ", ", "all", false, true )# )
</cfquery>
<cfquery name="q4" dbtype="query">
	select id from qs where 'a' in ( #listQualify( "a b, c", "'", ", " )# )
</cfquery>
<cfoutput>#q1.recordCount#|#q2.recordCount#|#q3.recordCount#|#q4.recordCount#</cfoutput>
