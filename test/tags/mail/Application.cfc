component {
	this.mailservers = [ {
		"#form.serverField#": "localhost",
		port: #form.port#,
		ssl: false,
		tls: false,
		lifeTimespan: createTimeSpan(0,0,1,0),
		idleTimespan: createTimeSpan(0,0,0,10)
	} ];
}