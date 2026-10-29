component {

	this.name = "LDEV3570";

	function onError( exception, eventName ) {
		if ( url.keyExists( "onErrorThrows" ) ) {
			throw( message="LDEV3570 onError itself failed" );
		}
		// the return value must have no meaning
		writeOutput( "handled" );
		return url.keyExists( "returnFalse" ) ? false : true;
	}

}
