component {
	this.setClientCookies = false;
	this.sessionManagement = false;
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

	function onSessionStart() {
		// this is needed to force cookie creation
		//echo("session started");
	}
}