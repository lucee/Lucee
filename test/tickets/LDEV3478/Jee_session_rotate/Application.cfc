component {
	this.name="ldev3478_onsessionend_jee_sessionRotate_#(url.rotateOnSessionStart ?: "index")#-";
	this.sessionManagement = true;
	this.sessionStorage="memory";
	this.sessiontimeout="#createTimeSpan(0,0,0,1)#";
	this.setclientcookies="yes";
	this.applicationtimeout="#createTimeSpan(0,0,0,10)#";
	this.sessionType="jee";

	function onApplicationStart(){
		//systemOutput("application start #cgi.SCRIPT_NAME# - #this.name#", true);
	}

	function onApplicationEnd(){
		//systemOutput("#now()# application end #cgi.SCRIPT_NAME# - #this.name#", true);
	}

	function onSessionStart() {
		server.LDEV3478.start_JEE_Sessions[ session.sessionid ] = now();
		if ( structKeyExists( url, "rotateOnSessionStart" ) ){
			//systemOutput( "rotateOnSessionStart #cgi.SCRIPT_NAME#", true );
			sessionRotate();
			server.LDEV3478.start_JEE_Sessions[ session.sessionid ] = now();
		}
		//systemOutput( "session started #cgi.SCRIPT_NAME#", true );
		session.started = now();
	}

	function onSessionEnd( sessionScope, applicationScope ) {
		//systemOutput( "session end #cgi.SCRIPT_NAME#", true );
		server.LDEV3478.ended_JEE_Sessions[ arguments.sessionScope.sessionid ] = now();
	}

	public function onRequestStart() {
		setting requesttimeout=10;
	}

}