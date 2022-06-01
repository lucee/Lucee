component {
	this.name = 'LDEV2902';

	this.datasources["testTimezone"] = {
		  class: 'org.h2.Driver'
		, bundleName: 'org.h2'
		, bundleVersion: '1.3.172'
		, connectionString: 'jdbc:h2:'&expandpath('./test')&'\testTimezone;MODE=MySQL'
		, username: ''
		, password: ""	
		, timezone:'America/Chicago'

	};

	this.datasources["testNoTimezone"] = {
		  class: 'org.h2.Driver'
		, bundleName: 'org.h2'
		, bundleVersion: '1.3.172'
		, connectionString: 'jdbc:h2:'&expandpath('./test')&'\testNoTimezone;MODE=MySQL'
		, username: ''
		, password: ""

	};

	this.datasources["testEmptyTimezone"] = { 
		  class: 'org.h2.Driver'
		, bundleName: 'org.h2'
		, bundleVersion: '1.3.172'
		, connectionString: 'jdbc:h2:'&expandpath('./test')&'\testEmptyTimezone;MODE=MySQL'
		, username: ''
		, password: ""
		, timezone: ""

	};
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

}