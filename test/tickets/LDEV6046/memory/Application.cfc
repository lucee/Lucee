component {
	this.name = "ldev-6046-memory-" & hash( getCurrentTemplatePath() );
	this.sessionManagement = true;
	this.sessionTimeout = createTimeSpan( 0, 0, 5, 0 );
	this.setClientCookies = true;

	// Use memory for session storage - this should work correctly
	this.sessionStorage = "memory";

	public function onRequestStart() {
		setting requesttimeout=10 showdebugOutput=false;
	}

	function onSessionStart() {
		systemOutput("------onSessionStart (memory)", true);
		session.started = now();
	}

	function onSessionEnd( sessionScope, applicationScope ) {
		systemOutput("------onSessionEnd (memory)", true);
		server.LDEV6046_ended_sessions[ arguments.sessionScope.sessionid ] = now();
	}
}
