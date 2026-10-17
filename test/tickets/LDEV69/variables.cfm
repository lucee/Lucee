<cfscript>
variables.delete("k");
param variables["k"] default="v";
writeOutput(variables.k);
</cfscript>
