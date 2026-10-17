<cfscript>
form.delete("x");
param form["x"] = "hello";
writeOutput(form.x);
</cfscript>
