<cfscript>
	// the template path is relative to this template (LDEV4038/caller), not to the test case that requested it
	writeOutput( _internalRequest( template: "../sub/index.cfm" ).filecontent.trim() );
</cfscript>
