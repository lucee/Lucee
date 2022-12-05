component {
	this.name = 'LDEV4285';
	this.ORMenabled = "true";
	this.ORMsettings = {
		datasource = "LDEV4285",
		dbCreate = "dropCreate",
		dialect = " MySQL"
	}
	this.datasource = "LDEV4285";
	this.datasources["LDEV4285"] = {
		class: 'org.h2.Driver'
		, bundleName: 'org.h2'
		, bundleVersion: '1.3.172'
		, connectionString: 'jdbc:h2:'&getDirectoryFromPath(getCurrentTemplatePath())&'test'&'\testTimezone;MODE=MySQL'
	};
}