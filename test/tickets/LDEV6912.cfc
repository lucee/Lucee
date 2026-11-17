component extends="org.lucee.cfml.test.LuceeTestCase" labels="datasource" {

	function run( testResults, testBox ) {
		describe( "LDEV-6909 / LDEV-6912 datasource with bundleName but no bundleVersion uses the installed JDBC driver", function() {

			it( title="bundleName without bundleVersion resolves to the registered Maven driver", skip=!hasMavenDriver( "org.postgresql.Driver" ), body=function() {
				var registered = getRegisteredDriver( "org.postgresql.Driver" );
				var resolved = resolve( newClassDefinition( "org.postgresql.Driver", "org.postgresql.jdbc", nullValue() ) );
				expect( resolved.isMaven() ).toBeTrue();
				expect( resolved.toString() ).toBe( registered.cd.toString() );
			});

			it( title="bundleName with bundleVersion is not changed", skip=!hasMavenDriver( "org.postgresql.Driver" ), body=function() {
				var cd = newClassDefinition( "org.postgresql.Driver", "org.postgresql.jdbc", "42.7.11" );
				var resolved = resolve( cd );
				expect( resolved.isMaven() ).toBeFalse();
				expect( resolved.getName() ).toBe( "org.postgresql.jdbc" );
				expect( resolved.getVersionAsString() ).toBe( "42.7.11" );
			});

			it( title="a versionless bundle for a class no extension registered is not changed", body=function() {
				var cd = newClassDefinition( "org.example.ldev6912.Driver", "org.example.ldev6912", nullValue() );
				var resolved = resolve( cd );
				expect( resolved.isMaven() ).toBeFalse();
				expect( resolved.getName() ).toBe( "org.example.ldev6912" );
				expect( isNull( resolved.getVersion() ) ).toBeTrue();
			});

			it( title="a class only definition still resolves to the registered driver (LDEV-6908)", skip=!hasMavenDriver( "org.postgresql.Driver" ), body=function() {
				var registered = getRegisteredDriver( "org.postgresql.Driver" );
				var resolved = resolve( newClassDefinition( "org.postgresql.Driver", nullValue(), nullValue() ) );
				expect( resolved.toString() ).toBe( registered.cd.toString() );
			});

		});
	}

	private function resolve( cd ) {
		return createObject( "java", "lucee.runtime.config.ConfigFactoryImpl" ).resolveJDBCDriverClassDefinition( getPageContext().getConfig(), arguments.cd );
	}

	private function newClassDefinition( required string className, bundleName, bundleVersion ) {
		return createObject( "java", "lucee.transformer.library.ClassDefinitionImpl" ).init(
			arguments.className,
			arguments.bundleName ?: nullValue(),
			arguments.bundleVersion ?: nullValue(),
			nullValue()
		);
	}

	private function getRegisteredDriver( required string className ) {
		return getPageContext().getConfig().getJDBCDriverByClassName( arguments.className, nullValue() );
	}

	private boolean function hasMavenDriver( required string className ) {
		var driver = getRegisteredDriver( arguments.className );
		if ( isNull( driver ) ) return false;
		return createObject( "java", "lucee.transformer.library.ClassDefinitionImpl" ).isMaven( driver.cd );
	}

}
