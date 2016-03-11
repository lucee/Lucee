<cfquery>
UPDATE test SET name = NULL WHERE id = 1
</cfquery>
<cfscript>
OrmReload();
o = EntityLoad( "Test",1,true );
o.setName( "test1" );
transaction{
	EntitySave( o );
}
//Dump( var=o,showUdfs=false );
</cfscript>
<cfquery name="q">
SELECT * FROM test WHERE id = 1
</cfquery>
<!--- <cfdump var="#q#"> --->
<cfset newValueHasBeenSaved = ( q.name IS o.getName() )>
<cfoutput>#newValueHasBeenSaved#</cfoutput>