<cfscript>
	x = gethttprequestdata();
	writeDump(var=isbinary(x.content),output=expandpath("./a.txt"),format="text");
</cfscript>
