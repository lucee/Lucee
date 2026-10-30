<cfset q = request.ldev1791.q>
<cfoutput query="q" group="cat"><cfoutput group="artNr" groupCaseSensitive="#request.ldev1791.cs#">[#cat#/#artNr#]</cfoutput></cfoutput>
