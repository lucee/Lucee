component {

	function init() {
		variables.counts = {};
		return this;
	}

	function getCount( required string id ) {
		return variables.counts[ arguments.id ] ?: 0;
	}

	function slow( required string id ) cachedWithin="#createTimeSpan( 0, 0, 5, 0 )#" {
		lock name="ldev1360counter" timeout="5" { variables.counts[ arguments.id ] = ( variables.counts[ arguments.id ] ?: 0 ) + 1; }
		sleep( 800 );
		writeOutput( "out-#arguments.id#" );
		return "value-#arguments.id#";
	}

	// throws for the first call, works afterwards
	function failFirst( required string id ) cachedWithin="#createTimeSpan( 0, 0, 5, 0 )#" {
		var n = 0;
		lock name="ldev1360counter" timeout="5" {
			variables.counts[ arguments.id ] = ( variables.counts[ arguments.id ] ?: 0 ) + 1;
			n = variables.counts[ arguments.id ];
		}
		sleep( 500 );
		if ( n == 1 ) throw( message="boom", type="ldev1360" );
		return "value-#arguments.id#";
	}

	// calls itself with the same arguments
	function recursive( required numeric n ) cachedWithin="#createTimeSpan( 0, 0, 5, 0 )#" {
		if ( arguments.n > 0 ) return recursive( arguments.n );
		return "end";
	}
}
