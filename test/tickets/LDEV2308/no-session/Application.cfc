component {
	this.setClientCookies = false;
	this.sessionManagement = false;

	function onSessionStart() {
		// this is needed to force cookie creation
		//echo("session started");
	}
}