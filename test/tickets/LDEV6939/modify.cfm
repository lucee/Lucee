<cfscript>
	session.user.setUsername( url.username );
	echo( serializeJSON( { cfid: session.cfid, cftoken: session.cftoken } ) );
</cfscript>
