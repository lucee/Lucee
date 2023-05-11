component {
	this.name="cfml-session-defaults-ldev-3448";
	this.sessionManagement = true;
	this.sessionStorage="memory";
	this.sessiontimeout="#createTimeSpan(0,0,0,1)#";
	this.setclientcookies="yes";
	this.applicationtimeout="#createTimeSpan(0,0,0,10)#";
	this.sessionType="cfml";

	// these should be  the new defaults for 6.0
	// TODO remove once the changes are made
	this.sessionCookie.httpOnly = true; // prevent access to session cookies from javascript
	this.sessionCookie.sameSite = "lax";

	function onApplicationStart(){
		//systemOutput("application start #cgi.SCRIPT_NAME#", true);
	}

	function onApplicationEnd(){
		//systemOutput("#now()# application end #cgi.SCRIPT_NAME#", true);
	}

	function onSessionStart() {
		//systemOutput( "session started #cgi.SCRIPT_NAME#", true );
		session.started = now();
	}

	function onSessionEnd(SessionScope, ApplicationScope) {
		// systemOutput("#now()# session ended #cgi.SCRIPT_NAME# #sessionScope.sessionid#", true);
	}
	
	public function onRequestStart() {
	}

}