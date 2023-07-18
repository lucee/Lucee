component {
	public function init (){
		return this;
	}
	// strips off the stack trace to exclude testbox and back to the first .cfc call in the stack
	public static array function trimJavaStackTrace( required string st ){
		local.tab = chr( 9 );
		local.stack = [];
		local.i = find( "/testbox/", arguments.st );
		if ( request.testDebug ?: false || i eq 0 ){ // dump it all out
			arrayAppend( stack, TAB & arguments.st );
			return stack;
		}
		local.tmp = mid( arguments.st, 1, i ); // strip out anything after testbox
		local.tmp2 = reverse( local.tmp );
		local.i = find( ":cfc.", local.tmp2 ); // find the first cfc line
		if ( local.i gt 0 ){
			local.i = len( local.tmp )-i;
			local.j = find( ")", local.tmp, local.i ); // find the end of the line
			local.tmp = mid( local.tmp, 1, local.j );
		} else { 
			// no CFC reference found just, return the whole stack
			arrayAppend( stack, TAB & arguments.st );
			return stack;
		}
		arrayAppend( stack, TAB & local.tmp );
		// now find any Caused By: and output them
		local.tail = mid( arguments.st, local.j );
		local.firstCausedBy = findNoCase( "Caused by:", tail );
		if ( firstCausedBy gt 0 ) {
			arrayAppend( stack, TAB & TAB & TAB & "... omitted verbose (ant / pagecontext / testbox) default stacktraces ... " );
			arrayAppend( stack, mid( tail, firstCausedBy) );
		}
		return stack;
	}

}
