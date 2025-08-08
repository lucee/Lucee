component {
	this.name="ldev5756";
	this.logs = {
		"ldev5756-pattern": {
			"appender": "resource",
			"appenderArguments": {
				"path": "{lucee-config}/logs/ldev5756-pattern.log"
			},
			"level": "info",
			"layout": "pattern",
			"layoutArguments": {
				"pattern": "%d{yyyy-MM-dd HH:mm:ss,SSS} [%t] %-5p %c - %m%n"
			}
		},
		 "ldev5756-classic": {
			"appender":"resource",
			"appender-arguments": {
				"path" :"{lucee-config}/logs/ldev5756-classic.log"
			},
			"layout":"classic",
			"level":"info",
			"name":"application"
		},
		"ldev4153-classic": {
			"appender":"resource",
			"appender-arguments":"path:{lucee-config}/logs/ldev4153-classic.log",
			"layout":"classic",
			"level":"info",
			"name":"application"
		}
	}
	
};