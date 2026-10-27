component extends="org.lucee.cfml.test.LuceeTestCase" labels="config" {

	function beforeAll() {
		variables.csClass = createObject( "java", "lucee.runtime.config.ConfigServerImpl" );
		variables.cs = createObject( "java", "lucee.runtime.config.ConfigUtil" ).getConfigServerImpl( getPageContext().getConfig() );
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-6749 dotNotationUpperCase in .CFConfig.json is ignored", skip=isEnvDefined(), body=function() {

			it( "dotNotationUpperCase=false preserves the case", function() {
				expect( readPreserveCase( { "dotNotationUpperCase": false } ) ).toBeTrue();
			});

			it( "dotNotationUpperCase=true upper cases", function() {
				expect( readPreserveCase( { "dotNotationUpperCase": true } ) ).toBeFalse();
			});

			it( "dotNotationUpperCase as string", function() {
				expect( readPreserveCase( { "dotNotationUpperCase": "false" } ) ).toBeTrue();
				expect( readPreserveCase( { "dotNotationUpperCase": "true" } ) ).toBeFalse();
			});

			it( "preserveCase takes precedence over dotNotationUpperCase", function() {
				expect( readPreserveCase( { "preserveCase": false, "dotNotationUpperCase": false } ) ).toBeFalse();
				expect( readPreserveCase( { "preserveCase": true, "dotNotationUpperCase": true } ) ).toBeTrue();
			});

			it( "preserveCase alone still works", function() {
				expect( readPreserveCase( { "preserveCase": true } ) ).toBeTrue();
				expect( readPreserveCase( { "preserveCase": false } ) ).toBeFalse();
			});

			it( "default upper cases", function() {
				expect( readPreserveCase( {} ) ).toBeFalse();
			});

		});
	}

	// a system property / environment variable [lucee.preserve.case] overrules the config file
	private boolean function isEnvDefined() {
		return !isNull( createObject( "java", "lucee.runtime.config.ConfigServerImpl" ).metaPreserveCase.getEnvVarSystemPropSource() );
	}

	private boolean function readPreserveCase( required struct root ) {
		return variables.csClass.readPreserveCase( variables.cs, arguments.root );
	}
}
