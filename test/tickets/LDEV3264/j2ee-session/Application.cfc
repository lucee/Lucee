component {
	this.sessionManagement = true;
	this.sessionStorage="memory";
	this.sessiontimeout="#createTimeSpan(0,0,0,1)#";
	this.setclientcookies="yes";
	this.applicationtimeout="#createTimeSpan(0,0,0,10)#";
	this.name="onsessionend-j2ee";
	this.sessionType="j2ee";

	function onApplicationStart(){
		systemOutput("application start #cgi.SCRIPT_NAME#", true);
	}

	function onApplicationEnd(){
		systemOutput("#now()# application end #cgi.SCRIPT_NAME#", true);
	}

	function onSessionStart() {
		systemOutput("session started #cgi.SCRIPT_NAME#", true);
		session.started = now();
	}

	function onSessionEnd(SessionScope, ApplicationScope) {
		systemOutput("#now()# session ended #cgi.SCRIPT_NAME# #sessionScope.sessionid#", true);
		server.LDEV3264_endedSessions[ arguments.sessionScope.sessionid ] = now();
	}
}