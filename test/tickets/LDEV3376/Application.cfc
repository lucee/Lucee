component {

	this.name = "LDEV3376-" & hash( getCurrentTemplatePath() );

	// the entries are defined for the host of the httpbin test service and for an alternative name of the same service
	variables.httpbin = server.getTestService( "httpbin" );
	if ( structCount( variables.httpbin ) ) {
		variables.host = variables.httpbin.server;
		variables.altHost = variables.host == "127.0.0.1" ? "localhost" : "127.0.0.1";

		// basic authentication, host:port wins over host
		this.vfs.http[ "#variables.host#:#variables.httpbin.port#" ] = { username: "susi", password: "sorglos" };
		this.vfs.http[ variables.host ] = { username: "wrong", password: "wrong" };

		// arbitrary headers, the host without the port is used for every port
		this.vfs.http[ variables.altHost ] = {
			headers: { "Authorization": "Bearer secret-token", "X-LDEV3376": "hello" }
		};
	}

	// not a struct
	this.vfs.http[ "invalid-entry.example" ] = "susi:sorglos";

}
