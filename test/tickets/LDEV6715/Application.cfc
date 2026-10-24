component {
	param name="url.proxy" default="false";

	this.name = "ldev6715-" & ( url.proxy ? "proxy" : "noproxy" );
	// nothing listens on this port, a download that goes through this proxy fails with a connect error naming it
	if ( url.proxy ) {
		this.proxy = {
			server: "127.0.0.1",
			port: 1
		};
	}
}
