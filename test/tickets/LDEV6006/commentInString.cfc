component {
	function test( line = "<!--- test --->" ) {
		if ( reFindNoCase( "^<!---.*--->$", line ) ) {
			return true;
		}
		return false;
	}
}
