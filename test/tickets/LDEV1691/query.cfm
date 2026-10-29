<cfset q = queryNew( "a", "varchar", [ [ url.v ], [ "x" ] ] )><cfoutput query="q">#q.a#;</cfoutput>
