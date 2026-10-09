component {
	this.name = "LDEV3204-" & hash( getCurrentTemplatePath() );
	// unquoted keys -> upper case keys (USERNAME / PASSWORD), as in the ticket
	this.mailservers = [ {
		  host: "127.0.0.1"
		, port: 2526
		, username: "MixedCaseUser"
		, password: "MixedCasePw"
		, ssl: false
		, tls: false
	} ];
}
