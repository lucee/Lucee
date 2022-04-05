component {
	this.name = "login";
	this.sessionManagement = true;
	this.sessionStorage="memory";
	this.sessiontimeout="#createTimeSpan(0,0,0,1)#";
	this.sessionType = "cfml";

	this.setclientcookies="yes";

	this.loginstorage = "session";
	
	this.sessionTimeout = createTimeSpan(0,0,0,1);

	function onSessionEnd(SessionScope, ApplicationScope) {
		systemOutput("#now()# session ended #cgi.SCRIPT_NAME# #sessionScope.sessionid#", true);
	}

}