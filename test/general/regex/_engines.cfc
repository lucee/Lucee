// Composition helper for regex bedrock tests. Underscore-prefixed so the test
// filter skips it as a non-test. Sub-CFCs hold an instance in variables._regex
// and dispatch testFoo() bodies through eachEngine() so each test runs once per
// engine in variables.ENGINES. Methods can declare `unsupportedRegexEngine="perl"`
// (csv) as a function attribute to skip listed engines.
// Bedrock supersedes LDEV-2892's broad perl-vs-java parity sweep.
component {

	variables.ENGINES = [
		{ label: "perl",   config: { type: "perl"   } },
		{ label: "java",   config: { type: "java"   } },
		{ label: "compat", config: { type: "compat" } }
	];

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
