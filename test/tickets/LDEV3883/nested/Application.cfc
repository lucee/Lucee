component {

	if ( isNull( url.appName ) ) url.appName = "";

	this.name = "#url.appName#";

	function onApplicationStart() {
		var key = "LDEV3883_nested_" & this.name;
		server[ key ] = ( server[ key ] ?: 0 ) + 1;
		// a nested request into the same application while it is starting must not start it again,
		// the cap only stops a runaway recursion if it does
		if ( server[ key ] lt 3 ) {
			internalRequest( template = url.template, url = { appName: this.name, template: url.template } );
		}
	}

}
