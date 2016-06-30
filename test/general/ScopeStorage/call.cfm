<cfscript>
	session["a-"&url.name]=url.value;
	sleep(url.time);
	session["b-"&url.name]=url.value;
	echo( getPageContext().getCFID() &":::::::::");
	//echo(serialize(cookie));
	//systemoutput(serialize(session),true);
</cfscript>