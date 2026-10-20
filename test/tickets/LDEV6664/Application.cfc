component {
	param name="url.id" default="";

	this.name = "ldev6664-" & url.id;
	// route the application log of this request to a file of its own, so the test can read it
	this.logs = {
		"application": {
			"appender": "resource",
			"appenderArguments": {
				"path": "{lucee-config}/logs/ldev6664-#url.id#.log"
			},
			"level": "info",
			"layout": "classic"
		}
	};
}
