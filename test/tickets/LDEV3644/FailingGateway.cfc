/**
 * event gateway used by LDEV3644, it fails to start as long as server.ldev3644FailStart is true
 */
component {

	public void function init( string id, struct config, component listener ) {
	}

	public void function start() {
		if ( server.keyExists( "ldev3644FailStart" ) && server.ldev3644FailStart ) {
			throw( message="LDEV-3644 gateway start failure", detail="LDEV-3644 failure detail" );
		}
	}

	public void function stop() {
	}

}
