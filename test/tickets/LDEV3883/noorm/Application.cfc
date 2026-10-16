component {

	if ( isNull( url.appName ) ) url.appName = "";
	if ( isNull( url.useAppScope ) ) url.useAppScope = false;

	this.name = "#url.appName#";
	this.sessionManagement = true;
	this.sessionTimeout = createTimespan( 0, 0, 0, 10 );

	if ( url.useAppScope ) {
		isNull( application.test ); // access application scope in pseudo constructor
	}

	function onApplicationStart() {
		writeoutput( "onApplicationStart," );
	}

	function onSessionStart() {
		writeoutput( "onSessionStart," );
	}

	function onRequestStart() {
		writeoutput( "onRequestStart," );
	}

	function onRequestEnd() {
		writeoutput( "onRequestEnd" );
	}

}
