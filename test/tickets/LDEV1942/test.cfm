<cfscript>
	SESSION.someVar = 'x';
	SESSION.someVar2 = 'y';
	structDelete(SESSION, "someVar");
	writeOutput(StructKeyExists(SESSION, "someVar"));
</cfscript>