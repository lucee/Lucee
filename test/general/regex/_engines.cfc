// Composition helper for regex bedrock tests. Underscore-prefixed so the test
// filter skips it as a non-test. Sub-CFCs hold an instance in variables._regex
// and dispatch testFoo() bodies through eachEngine() so each test runs once per
// engine in variables.ENGINES (the built in engines, other engines are added by the extension that provides them, see below).
// Methods can declare `unsupportedRegexEngine="compat"`
// (csv) as a function attribute to skip listed engines.
// Bedrock supersedes LDEV-2892's broad perl-vs-java parity sweep.
component {

	variables.ENGINES = [
		{ label: "java",   config: { type: "java"   } },
		{ label: "compat", config: { type: "compat" } }
	];

	// Engines that are provided by extensions are not part of the Lucee build, an extension can add its engine
	// with the environment variable LUCEE_TEST_REGEX_ENGINES (csv), for example "perl" for the Perl Regex
	// extension, which runs these tests in its own CI. Tests declared with unsupportedRegexEngine="java,compat"
	// only run for such an engine.
	if ( structKeyExists( server.system.environment, "LUCEE_TEST_REGEX_ENGINES" ) ) {
		for ( variables._engineName in listToArray( server.system.environment.LUCEE_TEST_REGEX_ENGINES ) ) {
			arrayAppend( variables.ENGINES, { label: trim( variables._engineName ), config: { type: trim( variables._engineName ) } } );
		}
	}

	public function setup( required any testInstance ) {
		variables._origEngine = getApplicationSettings().regex;
		variables._fnMeta = {};
		for ( var fn in getMetaData( arguments.testInstance ).functions ) {
			variables._fnMeta[ lcase( fn.name ) ] = fn;
		}
	}

	public function teardown() {
		application action="update" regex=variables._origEngine;
	}

	public function eachEngine( required any body ) {
		var caller = lcase( callStackGet()[ 2 ].function );
		var meta   = variables._fnMeta[ caller ] ?: {};
		var skip   = listToArray( lcase( meta.unsupportedRegexEngine ?: "" ) );

		for ( var engine in variables.ENGINES ) {
			if ( arrayLen( skip ) && arrayFind( skip, engine.label ) ) continue;

			application action="update" regex=engine.config;
			try {
				arguments.body( engine.label );
			} catch ( any e ) {
				throw( type="RegexBedrockFailure", message="engine=#engine.label#: #e.message#", cause=e );
			} finally {
				application action="update" regex=variables._origEngine;
			}
		}
	}
}
