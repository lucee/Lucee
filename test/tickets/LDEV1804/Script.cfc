component {

	function echo( required json data ) {
		return arguments.data;
	}

	string function echoUpper( required json data ) {
		return arguments.data;
	}

	json function returnValue( required any data ) {
		return arguments.data;
	}

	function many( required json[] items ) {
		return arrayLen( arguments.items );
	}

	function optional( json data = "[]" ) {
		return arguments.data;
	}

	function badDefault( json data = "not json" ) {
		return arguments.data;
	}

	function typeUpper( required JSON data ) {
		return arguments.data;
	}

}
