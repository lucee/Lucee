<cfset q = request.ldev1791.q>
<cfoutput><cfloop query="q" group="cat"><cfloop group="artNr" groupCaseSensitive="#request.ldev1791.cs#">[#cat#/#artNr#]</cfloop></cfloop></cfoutput>
