<cfscript>
	data = { name: "Susi", list: [ 1, 2 ] };
	switch ( url.fn ) {
		case "tag":
			cfdump( var=data );
			break;
		case "dump":
			dump( var=data );
			break;
		case "dumpPositional":
			dump( data );
			break;
		case "writeDump":
			writeDump( var=data );
			break;
		case "writeDumpPositional":
			writeDump( data );
			break;
		case "dumpHtml":
			dump( var=data, format="html" );
			break;
		case "writeDumpHtml":
			writeDump( var=data, format="html" );
			break;
		case "dumpBrowser":
			dump( var=data, output="browser" );
			break;
		case "writeDumpBrowser":
			writeDump( var=data, output="browser" );
			break;
	}
</cfscript>
