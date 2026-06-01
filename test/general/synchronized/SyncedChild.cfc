component extends="Synced" {

	// child of a synchronized component — sync flag must be inherited so the child
	// also serialises access on its own instance
	function bumpViaSuper( numeric n = 1 ) {
		return super.bump( n );
	}

}
