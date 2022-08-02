component {
	this.setClientCookies = false;
	this.sessionManagement = true;
	this.name="session-no-cookies";

	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

	function onSessionStart() {
		// this is needed to force cookie creation
		//echo("session started");
	}
}