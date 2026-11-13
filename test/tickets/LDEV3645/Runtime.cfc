component {

	function init( id, config, listener ) {
	}

	function getState() {
		return notDefinedVariable.foo; // ERROR:state
	}

	function start() {
		throw( message="LDEV3645 start failed" ); // ERROR:start
	}

}
