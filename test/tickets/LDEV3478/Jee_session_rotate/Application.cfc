component {
	this.name="onsessionend_jee_sessionRotate";
	this.sessionManagement = true;
	this.sessionStorage="memory";
	this.sessiontimeout="#createTimeSpan(0,0,0,1)#";
	this.setclientcookies="yes";
	this.applicationtimeout="#createTimeSpan(0,0,0,10)#";
	this.sessionType="jee";

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
		server.LDEV3478_ended_JEE_Sessions[ arguments.sessionScope.sessionid ] = now();
	}
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

}