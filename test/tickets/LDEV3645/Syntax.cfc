component {

	function init( id, config, listener ) {
		try {
			cfhttp( url="x" #bad# ); // ERROR:syntax
		}
		catch( any e ) {}
	}

	function getState() {
		return "stopped";
	}

}
