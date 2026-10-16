component synchronized="true" {

	function init( string tag = "synced-default" ) {
		this.tag = arguments.tag;
		this.callCount = 0;
		return this;
	}

	// counter increment without protection — if synchronized="true" actually serialises
	// access to the component, two threads incrementing concurrently won't lose updates
	function bump( numeric n = 1 ) {
		var oldCount = this.callCount;
		// brief yield to widen the race window if sync is broken
		sleep( 1 );
		this.callCount = oldCount + arguments.n;
		return this.callCount;
	}

}
