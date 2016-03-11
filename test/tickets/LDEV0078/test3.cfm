<cfquery>
UPDATE test SET name = NULL WHERE id = 1
</cfquery>
<cfscript>
OrmReload();
o = EntityLoad( "Test",1,true );// Loading outside the transaction: transaction will not be saved
transaction{
	//o = EntityLoad( "Test",1,true );// Loading inside the transaction: transaction will be saved
	o.setName( "test1" );
	//EntitySave( o );//will force the save regardless
	///ormFlush();//will force the save regardless
}
</cfscript>
<cfquery name="q">
SELECT * FROM test WHERE id = 1
</cfquery>
<cfset newValueHasBeenSaved = ( q.name IS o.getName() )>
<cfoutput>#newValueHasBeenSaved#</cfoutput>