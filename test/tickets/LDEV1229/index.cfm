<cfscript>
OrmReload();
parent = EntityLoadByPk( "Parent", 1 );
transaction{
	EntitySave( parent );
}
writeOutput(parent.getID());
</cfscript>