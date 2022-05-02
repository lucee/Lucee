component extends="org.lucee.cfml.test.LuceeTestCase"{
	
	function run( testResults , testBox ) {
		describe( "Checking JDBC drivers", function() {
			it( title='MySQL',skip=isNotSupported(), body=function( currentSpec ) {
				loadJDBCDriverClass("com.mysql.cj","8.0.11");
				loadJDBCDriverClass("com.mysql.cj","8.0.12");
				loadJDBCDriverClass("com.mysql.cj","8.0.13");
				loadJDBCDriverClass("com.mysql.cj","8.0.14");
				loadJDBCDriverClass("com.mysql.cj","8.0.15");
				loadJDBCDriverClass("com.mysql.cj","8.0.16");
				loadJDBCDriverClass("com.mysql.cj","8.0.17");
				loadJDBCDriverClass("com.mysql.cj","8.0.18");
				loadJDBCDriverClass("com.mysql.cj","8.0.19");
				loadJDBCDriverClass("com.mysql.cj","8.0.20");
				loadJDBCDriverClass("com.mysql.cj","8.0.21");
				loadJDBCDriverClass("com.mysql.cj","8.0.22");
				loadJDBCDriverClass("com.mysql.cj","8.0.23");
				loadJDBCDriverClass("com.mysql.cj","8.0.24");
				loadJDBCDriverClass("com.mysql.cj","8.0.25");
				loadJDBCDriverClass("com.mysql.cj","8.0.26");
				loadJDBCDriverClass("com.mysql.cj","8.0.27");
				loadJDBCDriverClass("com.mysql.cj","8.0.28");
			});

			it( title='MSSQL',skip=isNotSupported(), body=function( currentSpec ) {
				loadJDBCDriverClass("mssqljdbc4","4.0.2206.100");
				loadJDBCDriverClass("mssqljdbc4","4.1.5605.100");
				loadJDBCDriverClass("mssqljdbc4","4.2.6420.100");
				loadJDBCDriverClass("com.microsoft.sqlserver.mssql-jdbc","6.4.0.jre9");
				loadJDBCDriverClass("com.microsoft.sqlserver.mssql-jdbc","6.5.4");
				loadJDBCDriverClass("org.lucee.mssql","7.2.2.jre8");
				loadJDBCDriverClass("org.lucee.mssql","7.4.1.jre8");
				loadJDBCDriverClass("org.lucee.mssql","8.4.1.jre8");
				loadJDBCDriverClass("org.lucee.mssql","9.4.1.jre16");
			});

			it( title='Postgre',skip=isNotSupported(), body=function( currentSpec ) {
				loadJDBCDriverClass("org.lucee.postgresql","8.3.0.jdbc4");
				loadJDBCDriverClass("org.postgresql.jdbc42","9.4.1212");
				loadJDBCDriverClass("org.postgresql.jdbc42","42.1.4");
				loadJDBCDriverClass("org.postgresql.jdbc","42.2.17");
				loadJDBCDriverClass("org.postgresql.jdbc","42.2.20");
			});

			it( title='Exasol',skip=isNotSupported(), body=function( currentSpec ) {
				loadJDBCDriverClass("org.lucee.exasol","7.1.2");
			});

			it( title='Oracle',skip=isNotSupported(), body=function( currentSpec ) {
				loadJDBCDriverClass("ojdbc6","11.2.0.4L0001");
				loadJDBCDriverClass("ojdbc7","12.1.0.2L0001");
				loadJDBCDriverClass("org.lucee.oracle","19.12.0.0000L");
				loadJDBCDriverClass("org.lucee.oracle","21.3.0.0000L");
			});

			it( title='H2',skip=isNotSupported(), body=function( currentSpec ) {
				loadJDBCDriverClass("org.h2","1.3.172");
			});

			it( title='HSQLDB',skip=isNotSupported(), body=function( currentSpec ) {
				loadJDBCDriverClass("org.hsqldb.hsqldb","2.3.2");
			});

			it( title='JTDS',skip=isNotSupported(), body=function( currentSpec ) {
				loadJDBCDriverClass("jtds","1.2.5");
				loadJDBCDriverClass("jtds","1.3.1");
			});
		});
	}

	private function loadJDBCDriverClass(bundleName,bundleVersion) {
		var OSGiUtil=createObject("java","lucee.runtime.osgi.OSGiUtil");
		var IOUtil=createObject("java","lucee.commons.io.IOUtil");
		var bundle=OSGiUtil.loadBundle(bundleName,OSGiUtil.toVersion(bundleVersion), nullValue(), nullValue(), true);
		var driver=bundle.getResource("META-INF/services/java.sql.Driver");
		var is=driver.openStream();
		var className=trim(IOUtil.toString(is, "UTF-8"));
		return bundle.loadClass(className);
	}
}