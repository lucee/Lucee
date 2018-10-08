<cfscript>
	ormReload();
	obj = entityLoad("foo");
	writeOutput(obj[1].getLabel());
</cfscript>
