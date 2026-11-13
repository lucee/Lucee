component {
	this.name = "LDEV3320-cfml-" & hash( getCurrentTemplatePath() );
	this.sessionManagement = true;
	this.sessionStorage = "memory";
	this.sessionTimeout = createTimeSpan( 0, 0, 0, 1 );
	this.applicationTimeout = createTimeSpan( 0, 0, 0, 10 );
	this.setClientCookies = true;
	this.sessionType = "cfml";
	this.LDEV3320Marker = "this-scope-cfml";

	variables.LDEV3320Payload = repeatString( "x", 10000 );

	function onApplicationStart() {
		application.LDEV3320Marker = "application-scope-cfml";
	}

	function onSessionStart() {
		session.LDEV3320Started = now();
	}

	function onSessionEnd( SessionScope, ApplicationScope ) {
		var sessionId = arguments.sessionScope.sessionid;
		var previous = server.LDEV3320_ended[ sessionId ] ?: {};
		server.LDEV3320_ended[ sessionId ] = {
			"calls": ( previous.calls ?: 0 ) + 1,
			"hasSessionData": structKeyExists( arguments.sessionScope, "LDEV3320Started" ),
			"applicationName": arguments.applicationScope.applicationName,
			"applicationMarker": arguments.applicationScope.LDEV3320Marker ?: "",
			"thisMarker": this.LDEV3320Marker ?: ""
		};
	}
}
