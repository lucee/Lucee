<cfscript>
	base =  getDirectoryFromPath(getCurrentTemplatePath());
	writeOutput(isObject(Application.obj.tester()));
</cfscript>