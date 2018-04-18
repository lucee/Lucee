<cfscript>
	metaData1 = getComponentMetadata("someComponent");
	metaData2 = getComponentMetaData("abstractComponent");
	// writeDump(metaData1);
	writeOutput(ArrayLen(metaData2.functions));
</cfscript>