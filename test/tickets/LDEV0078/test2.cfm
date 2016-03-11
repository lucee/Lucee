<cfquery>
UPDATE test SET name = NULL WHERE id = 1
</cfquery>
<cfscript>
OrmReload();
o = EntityLoad( "Test",1,true );
transaction{
	o.setName( "test1" );
	//EntitySave( o );
	//ormFlush();
}
</cfscript>
<cfquery name="q">
SELECT * FROM test WHERE id = 1
</cfquery>
<cfset newValueHasBeenSaved = ( q.name IS o.getName() )>
<cfoutput>#newValueHasBeenSaved#</cfoutput>