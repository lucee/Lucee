component {

	function join( required string sep, ...parts ) {
		return arrayToList( parts, sep );
	}

	function count( ...all ) {
		return arrayLen( all );
	}

}
