<cfscript>
variables.test = EntityNew("Test");
variables.test.setID(2)
variables.test.setName(null)
EntitySave(variables.test);
writeOutput(SerializeJSON(variables.test));
</cfscript>