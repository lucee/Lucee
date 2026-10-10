component {

	variables.seen = [];
	variables.nullSeen = false;
	variables.removed = false;
	variables.hashes = [];
	variables.result = true;
	variables.removeOther = true;

	function init( required cache, required array keys, boolean result=true, boolean removeOther=true ) {
		variables.cache = arguments.cache;
		variables.keys = arguments.keys;
		variables.result = arguments.result;
		variables.removeOther = arguments.removeOther;
		return this;
	}

	boolean function accept( entry ) {
		if ( isNull( arguments.entry ) ) {
			variables.nullSeen = true;
			return false;
		}
		var current = arguments.entry.getKey();
		arrayAppend( variables.seen, current );
		arrayAppend( variables.hashes, createObject( "java", "java.lang.System" ).identityHashCode( arguments.entry.getValue() ) );

		// simulate an entry vanishing after keys() was listed: drop one key that has not been visited yet
		if ( variables.removeOther && !variables.removed ) {
			for ( var k in variables.keys ) {
				if ( k != current && !arrayFindNoCase( variables.seen, k ) ) {
					variables.cache.remove( k );
					variables.removed = true;
					break;
				}
			}
		}
		return variables.result;
	}

	// must not be "" or "*", otherwise CacheUtil.allowAll() bypasses the code under test
	string function toPattern() {
		return "ldev6546";
	}

	boolean function wasNullSeen() {
		return variables.nullSeen;
	}

	array function getHashes() {
		return variables.hashes;
	}

}
