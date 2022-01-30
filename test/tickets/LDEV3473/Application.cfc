component {
    this.name = "LDEV-3473";
    this.sessionManageMent = true;
	this.sessioncluster = true;
	this.sessionStorage = "testH2";

    this.datasources["testh2"] = {
		class: 'org.h2.Driver'
		, bundleName: 'org.h2'
		, bundleVersion: '1.3.172'
		, connectionString: 'jdbc:h2:#getTempDirectory()#/LDEV3473/datasource;MODE=MySQL'
		, username: ''
		, password: ""
		
		// optional settings
		, connectionLimit:100 // default:-1
		, liveTimeout:60 // default: -1; unit: minutes
		, storage:true // default: false
		, validate:false // default: false
	};

}