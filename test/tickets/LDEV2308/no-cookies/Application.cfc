component {
	this.setClientCookies = false;
	this.sessionManagement = true;
	this.name="session-no-cookies";

	function onSessionStart() {
		// this is needed to force cookie creation
		//echo("session started");
	}
}