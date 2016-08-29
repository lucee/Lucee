<cfscript>
	client["ca-"&url.name]=url.value;
	session["sa-"&url.name]=url.value;
	sleep(url.time);
	client["cb-"&url.name]=url.value;
	session["sb-"&url.name]=url.value;
	echo( getPageContext().getCFID() &":::::::::");
	//echo(serialize(cookie));
	//systemoutput(serialize(session),true);
</cfscript>