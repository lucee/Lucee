<cfscript>
	o1 = new returnThis();
	o2 = new returnVoid();
	o3 = new returnCustom();
	writeOutput( o1.value & "|" & o2.value & "|" & o3.custom );
</cfscript>
