component {
	this.sessionManagement = true;
	this.sessionStorage="memory";
	this.sessiontimeout="#createTimeSpan(0,0,0,10)#";
	this.setclientcookies="yes";
	this.applicationtimeout="#createTimeSpan(0,0,0,10)#";
	this.name="thread-session-cfml";
	this.sessionType="cfml"; // lucee default
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

	function onSessionStart() {
		// this is needed to force cookie creation
		//echo("session started");
	}
}