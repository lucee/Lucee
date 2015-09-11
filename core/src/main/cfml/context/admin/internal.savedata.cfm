<cfif listFind("addfavorite,removefavorite", url.action2) and structKeyExists(url, "favorite")>
	<cfset application.adminfunctions[url.action2](url.favorite) />
	<cflocation url="?action=#url.favorite#" addtoken="no" />
<cfelseif listFind("setdata,adddata", url.action2) and structKeyExists(url, "key")>
	<cfset application.adminfunctions[url.action2](url.key, url.data) />
	<cfabort />
</cfif>

<cflocation url="#cgi.SCRIPT_NAME#" addtoken="no" />