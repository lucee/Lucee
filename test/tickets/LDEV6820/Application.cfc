component {
	this.name = "ldev6820";
	logDir = getDirectoryFromPath( getCurrentTemplatePath() ) & "logs/";
	mvn = "lucee.test.ldev6820:logging:1.0";
	this.logs = {
		"ldev6820-appender": {
			"appender": { "class": "lucee.test.FileAppender6820", "maven": mvn },
			"appenderArguments": { "file": logDir & "appender.log" },
			"layout": "classic",
			"level": "info"
		},
		"ldev6820-layout": {
			"appender": "resource",
			"appenderArguments": { "path": logDir & "layout.log" },
			"layout": { "class": "lucee.test.Layout6820", "maven": mvn },
			"level": "info"
		}
	};
}
