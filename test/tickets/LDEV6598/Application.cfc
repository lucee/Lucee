component {
	param name="url.id" default="";
	param name="url.tz" default="UTC";

	this.name = "ldev6598-" & url.id;
	// request/application timezone, deliberately different from the server config timezone
	this.timezone = url.tz;
	this.logs = {
		"ldev6598-pattern-#url.id#": {
			"appender": "resource",
			"appenderArguments": {
				"path": "{lucee-config}/logs/ldev6598-pattern-#url.id#.log"
			},
			"level": "info",
			// no pattern, so the default pattern of the pattern layout is used
			"layout": "pattern"
		},
		"ldev6598-datadog-#url.id#": {
			"appender": "resource",
			"appenderArguments": {
				"path": "{lucee-config}/logs/ldev6598-datadog-#url.id#.log"
			},
			"level": "info",
			"layout": "datadog"
		}
	};
}
