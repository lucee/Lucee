<cfset q = request.ldev1791.q>
<cfoutput query="q" group="artNr" groupCaseSensitive="#request.ldev1791.cs#">{#artNr#|<cfoutput>#id#;</cfoutput>}</cfoutput>
