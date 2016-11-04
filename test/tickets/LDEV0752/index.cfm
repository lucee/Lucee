<cfscript>
	param name="FORM.Scene" default="1";

	// changing the content of SupportTicket.CFC as per the ticket requirement
	currPath = expandPath("./");
	currCFCPath = currPath&"SupportTicket.cfc";
	currFilePath = currPath&"case#FORM.Scene#.txt";
	fileWrite(currCFCPath, fileRead(currFilePath));

	try{
		// Refreshing ORM objects
		ormReload();
	}catch( any e ){
		writeOutput( e.Message );
	}
</cfscript>