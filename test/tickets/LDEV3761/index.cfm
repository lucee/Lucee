<cfscript>
if(isNull(url.type))url.type="";


if(url.type=="abort") abort;
if(url.type=="exception") throw "upsi dupsi!";
</cfscript>