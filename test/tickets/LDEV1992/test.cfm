<cfscript>
entity = entityLoad( "users", {FirstName="lucee"}, true );
ormclearsession();
EntityMerge( entity ); //merge entity 
writeOutput(ormgetsession().getStatistics().getEntityCount());

</cfscript>