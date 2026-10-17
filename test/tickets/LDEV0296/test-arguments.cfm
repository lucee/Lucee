<cfscript>
	o1 = new withArgs( "Zac", 35, "Australia" );
	o2 = new withArgs( name="Zac", age=35, country="Australia" );
	writeOutput( o1.name & "," & o1.age & "|" & o2.name & "," & o2.age );
</cfscript>
