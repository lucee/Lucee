component {

	variables.cacheName = "ldev5922_not_existing_cache";

	// exact pattern from the ticket
	public string function ticketPattern() {
		var keys = [ "a", "b" ];
		try {
			echo( "" );
		}
		finally {
			for ( var k in keys ) {
				try {
					cacheDelete( id=k, cacheName=variables.cacheName );
				}
				catch ( any e ) {}
			}
		}
		return "ok";
	}

	// for-in inside finally, on the normal path and on the exception path
	public string function forInInFinally( boolean throwInTry=false ) {
		var keys = [ "a", "b" ];
		var done = [];
		try {
			if ( arguments.throwInTry ) throw "LDEV-5922 try";
		}
		catch ( any e ) {
			arrayAppend( done, "catch" );
		}
		finally {
			for ( var k in keys ) {
				try {
					arrayAppend( done, k );
				}
				catch ( any e ) {}
			}
		}
		return arrayToList( done );
	}

	// while loop inside finally
	public string function whileInFinally() {
		var done = [];
		try {
			echo( "" );
		}
		finally {
			while ( arrayLen( done ) < 2 ) {
				try {
					arrayAppend( done, arrayLen( done ) + 1 );
				}
				catch ( any e ) {}
			}
		}
		return arrayToList( done );
	}

	// break / continue in a loop inside finally
	public string function breakContinueInFinally() {
		var keys = [ "a", "b", "c", "d" ];
		var done = [];
		try {
			echo( "" );
		}
		finally {
			for ( var k in keys ) {
				if ( k == "b" ) continue;
				if ( k == "d" ) break;
				arrayAppend( done, k );
			}
		}
		return arrayToList( done );
	}
}
