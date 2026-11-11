component extends="org.lucee.cfml.test.LuceeTestCase" labels="datasource,jdbc,postgres" {

	// LDEV-6908 a datasource that only defines the driver class must use the registered JDBC driver,
	// also when the JDBC extension registers its driver as Maven artifact instead of an OSGi bundle

	function beforeAll() {
		variables.driverClass = "org.postgresql.Driver";
		variables.pg = structKeyExists( server, "getDatasource" ) ? server.getDatasource( "postgres" ) : {};
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6908 class only datasource definition", function() {

			it( title="application datasource with only [class] resolves to the registered JDBC driver", skip=!hasRegisteredDriver(), body=function( currentSpec ) {
				var ds = createObject( "java", "lucee.runtime.listener.AppListenerUtil" ).toDataSource(
					getPageContext().getConfig(),
					"ldev6908",
					{ class: variables.driverClass, connectionString: "jdbc:postgresql://localhost:5432/ldev6908" },
					nullValue()
				);
				var cd = ds.getClassDefinition();
				expect( cd.getClassName() ).toBe( variables.driverClass );
				expect( hasLoadInfo( cd ) ).toBeTrue( "class definition [#cd.toString()#] has neither bundle nor maven coordinates" );
			});

			it( title="query with an application datasource with only [class]", skip=( structCount( variables.pg ) == 0 ), body=function( currentSpec ) {
				var ds = {
					class: variables.driverClass,
					connectionString: variables.pg.connectionString,
					username: variables.pg.username,
					password: variables.pg.password
				};
				var q = queryExecute( "select 6908 as id", {}, { datasource: ds } );
				expect( q.id ).toBe( 6908 );
			});

		});
	}

	private boolean function hasRegisteredDriver() {
		var jdbc = getPageContext().getConfig().getJDBCDriverByClassName( variables.driverClass, nullValue() );
		return !isNull( jdbc ) && !isNull( jdbc.cd );
	}

	private boolean function hasLoadInfo( cd ) {
		if ( cd.isBundle() ) return true;
		return isInstanceOf( cd, "lucee.transformer.library.ClassDefinitionImpl" ) && cd.isMaven();
	}
}
