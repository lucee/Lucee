component {

	function init( string tag = "boom-default" ) {
		this.tag = arguments.tag;
		return this;
	}

	// throws from inside the base — the exception's component context should reference
	// the calling instance (the child), not the base. Today's behaviour is what we pin.
	function boom() {
		throw( type="LDEV6300.Boom", message="boom from #this.tag#" );
	}

}
