component {

	/**
	 * implementation of lucee.commons.io.log.Log that records what is logged, only the logging methods are
	 * implemented
	 */
	function init( required messages ) {
		variables.messages = arguments.messages;
		return this;
	}

	function log() {
		variables.messages.add( arguments[ 1 ] & "|" & arguments[ 2 ] & "|" & ( arguments[ 3 ] ?: "" ) );
	}

	function warn() {
		variables.messages.add( "3|" & arguments[ 1 ] & "|" & ( arguments[ 2 ] ?: "" ) );
	}

	function getLogLevel() {
		return 1;
	}

}
