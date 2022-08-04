<!--- this Application.cfc is invalid due to no component { } or <cfcomponent --->
<cfscript>
	this.name="cfmail-test-ldev-4052";
	this.mailservers = [ {
		host: "localhost"
		port: 25
		username: "demo"
		password: "meh",
		ssl: false,
		tls: false,
		lifeTimespan: createTimeSpan(0,0,1,0),
		idleTimespan: createTimeSpan(0,0,0,10)
	} ];
</cfscript>