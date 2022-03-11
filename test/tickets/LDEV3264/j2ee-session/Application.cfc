component {
	this.sessionManagement = true;
	this.sessionStorage="memory";
	this.sessiontimeout="#createTimeSpan(0,0,0,1)#";
	this.setclientcookies="yes";
	this.applicationtimeout="#createTimeSpan(0,0,0,10)#";
	this.name="onsessionend-j2ee";
	this.sessionType="j2ee";

	function onApplicationStart(){
		application.endedSessions = {};
	}

	function onSessionStart() {
		systemOutput("", true);
		systemOutput("session started #cgi.SCRIPT_NAME#", true);
		session.started = now();
	}

	function onSessionEnd(SessionScope, ApplicationScope) {
		systemOutput("", true);
		systemOutput("session ended #cgi.SCRIPT_NAME# #sessionScope.sessionid#", true);
		arguments.ApplicationScope.endedSessions[ arguments.sessionScope.sessionid ] = now();
	}
}