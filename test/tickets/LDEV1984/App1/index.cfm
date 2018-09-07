<cfscript>
	ormReload();
	obj = entityLoad("foo");
	ORMEvictEntity("foo");
	writeOutput(obj[1].getLabel());
</cfscript>
