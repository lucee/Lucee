component {
	this.name = 'LDEV2902';
    this.ORMenabled = "true";
    this.ORMsettings = {
        datasource = "LDEV3430",
        dbCreate = "dropCreate",
        dialect = " MySQL"
    }
    this.datasource = "LDEV3430";
	this.datasources["LDEV3430"] = {
		  class: 'org.h2.Driver'
		, bundleName: 'org.h2'
		, bundleVersion: '1.3.172'
		, connectionString: 'jdbc:h2:'&getDirectoryFromPath(getCurrenttemplatepath())&'test'&'\testTimezone;MODE=MySQL'
	};
}