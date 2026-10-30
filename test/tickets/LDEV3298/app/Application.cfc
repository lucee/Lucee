component {

	this.name = "LDEV3298";

	function onRequestStart( target ) {
		echo( "[ONREQUESTSTART]" );
		return true;
	}

	function onRequestEnd( target ) {
		echo( "[ONREQUESTEND]" );
	}

	function onAbort( target ) {
		echo( "[ONABORT]" );
	}

}
