component extends="org.lucee.cfml.test.LuceeTestCase" labels="date,config" {

	variables.sysprop = "lucee.date.parser";

	function beforeAll() {
		variables.System = createObject( "java", "java.lang.System" );
		variables.cs = createObject( "java", "lucee.runtime.config.ConfigUtil" ).getConfigServerImpl( getPageContext().getConfig() );
		variables.DateCaster = createObject( "java", "lucee.runtime.op.date.DateCaster" );
		variables.orgLocale = getLocale();
		variables.orgSysprop = System.getProperty( sysprop );
		variables.orgRaw = cs.raw().dateParser ?: "";
	}

	function afterAll() {
		setLocale( orgLocale );
		restore();
	}

	function run( testResults, testBox ) {
		describe( "LDEV-7006 setting to choose the default string-to-date parser", function() {

			describe( "default (simple parser)", function() {

				it( "the server default is the simple parser", function() {
					expect( cs.getDateParser() ).toBe( DateCaster.PARSER_SIMPLE );
					expect( getApplicationSettings().dateParser ).toBe( "simple" );
				});

				it( "en_US reads nn/nn/yyyy month first", function() {
					var res = callApp( locale: "en_US" );
					expectMonthFirst( res );
					expect( res.dateParser ).toBe( "simple" );
				});

				it( "en_GB still reads nn/nn/yyyy month first", function() {
					var res = callApp( locale: "en_GB" );
					expectMonthFirst( res );
					expect( res.dateParser ).toBe( "simple" );
				});

				it( "in the current request with en_GB", function() {
					try {
						setLocale( "en_GB" );
						expect( dateFormat( parseDateTime( "02/01/2026" ), "yyyy-mm-dd" ) ).toBe( "2026-02-01" );
						expect( dateTimeFormat( "02/01/2026 03:04", "yyyy-mm-dd HH:nn" ) ).toBe( "2026-02-01 03:04" );
						expect( month( "02/01/2026" ) ).toBe( 2 );
					}
					finally {
						setLocale( orgLocale );
					}
				});
			});

			describe( "Application.cfc this.dateParser", function() {

				it( "locale with en_GB reads nn/nn/yyyy day first", function() {
					var res = callApp( locale: "en_GB", appParser: "locale" );
					expect( res.dateParser ).toBe( "locale" );
					expectDayFirst( res );
				});

				it( "locale with en_US still reads nn/nn/yyyy month first", function() {
					var res = callApp( locale: "en_US", appParser: "locale" );
					expect( res.dateParser ).toBe( "locale" );
					expectMonthFirst( res );
				});

				it( "simple with en_GB reads nn/nn/yyyy month first", function() {
					var res = callApp( locale: "en_GB", appParser: "simple" );
					expect( res.dateParser ).toBe( "simple" );
					expectMonthFirst( res );
				});

				it( "overrides the server setting", function() {
					try {
						System.setProperty( sysprop, "locale" );
						cs.resetDateParser();
						var res = callApp( locale: "en_GB", appParser: "simple" );
						expect( res.dateParser ).toBe( "simple" );
						expectMonthFirst( res );
					}
					finally {
						restore();
					}
				});

				it( "is case insensitive", function() {
					var res = callApp( locale: "en_GB", appParser: "LOCALE" );
					expect( res.dateParser ).toBe( "locale" );
					expectDayFirst( res );
				});
			});

			describe( "tag application dateParser", function() {

				it( "locale with en_GB reads nn/nn/yyyy day first", function() {
					var res = callApp( locale: "en_GB", tagParser: "locale" );
					expect( res.dateParser ).toBe( "locale" );
					expectDayFirst( res );
				});

				it( "rejects an invalid value", function() {
					expect( function() {
						application action="update" dateParser="nope";
					}).toThrow();
				});
			});

			describe( ".CFConfig.json dateParser", function() {

				it( "locale is used by applications without their own setting", function() {
					try {
						cs.raw().dateParser = "locale";
						cs.resetDateParser();
						expect( cs.getDateParser() ).toBe( DateCaster.PARSER_LOCALE );
						var res = callApp( locale: "en_GB" );
						expect( res.dateParser ).toBe( "locale" );
						expectDayFirst( res );
					}
					finally {
						restore();
					}
				});

				it( "simple and unknown values are the simple parser", function() {
					try {
						cs.raw().dateParser = "simple";
						cs.resetDateParser();
						expect( cs.getDateParser() ).toBe( DateCaster.PARSER_SIMPLE );
						cs.raw().dateParser = "whatever";
						cs.resetDateParser();
						expect( cs.getDateParser() ).toBe( DateCaster.PARSER_SIMPLE );
					}
					finally {
						restore();
					}
				});
			});

			describe( "system property / environment variable", function() {

				it( "lucee.date.parser / LUCEE_DATE_PARSER selects the parser", function() {
					expect( createObject( "java", "lucee.commons.io.SystemUtil" ).convertSystemPropToEnvVar( sysprop ) ).toBe( "LUCEE_DATE_PARSER" );
					try {
						System.setProperty( sysprop, "locale" );
						cs.resetDateParser();
						expect( cs.getDateParser() ).toBe( DateCaster.PARSER_LOCALE );
						var res = callApp( locale: "en_GB" );
						expect( res.dateParser ).toBe( "locale" );
						expectDayFirst( res );
					}
					finally {
						restore();
					}
				});

				it( "takes precedence over .CFConfig.json", function() {
					try {
						cs.raw().dateParser = "simple";
						System.setProperty( sysprop, "locale" );
						cs.resetDateParser();
						expect( cs.getDateParser() ).toBe( DateCaster.PARSER_LOCALE );
					}
					finally {
						restore();
					}
				});

				it( "is documented", function() {
					var found = false;
					loop array=getSystemPropOrEnvVar() item="local.setting" {
						if ( setting.sysprop == sysprop ) {
							found = true;
							expect( setting.envvar ).toBe( "LUCEE_DATE_PARSER" );
							expect( setting.default ).toBe( "simple" );
						}
					}
					expect( found ).toBeTrue();
				});
			});
		});
	}

	// 02/01/2026 read month first (US), 1 February
	private function expectMonthFirst( required struct res ) {
		expect( res.parseDateTime ).toBe( "2026-02-01" );
		expect( res.parseDateTimeDateOnly ).toBe( "2026-02-01" );
		expect( res.dateTimeFormat ).toBe( "2026-02-01 03:04" );
		expect( res.dateFormat ).toBe( "2026-02-01" );
		expect( res.dateAdd ).toBe( "2026-02-02" );
		expect( res.day ).toBe( 1 );
		expect( res.month ).toBe( 2 );
		expect( res.isDateAmbiguous ).toBeTrue();
		expect( res.compare ).toBe( 1 );
		expectUnambiguous( res );
	}

	// 02/01/2026 read day first (en_GB), 2 January
	private function expectDayFirst( required struct res ) {
		expect( res.parseDateTime ).toBe( "2026-01-02" );
		expect( res.parseDateTimeDateOnly ).toBe( "2026-01-02" );
		expect( res.dateTimeFormat ).toBe( "2026-01-02 03:04" );
		expect( res.dateFormat ).toBe( "2026-01-02" );
		expect( res.lsDateFormat ).toBe( "2026-01-02" );
		expect( res.dateAdd ).toBe( "2026-01-03" );
		expect( res.day ).toBe( 2 );
		expect( res.month ).toBe( 1 );
		expect( res.isDate ).toBeTrue();
		expect( res.isDateAmbiguous ).toBeTrue();
		expect( res.compare ).toBe( 0 );
		expectUnambiguous( res );
	}

	// values the simple parser reads the same way with every setting
	private function expectUnambiguous( required struct res ) {
		expect( res.iso ).toBe( "2026-01-02 03:04:05" );
		expect( res.isoT ).toBe( "2026-01-02 03:04:05" );
		expect( res.ts ).toBe( "2026-01-02 03:04:05" );
		expect( res.monthName ).toBe( "2026-01-02" );
	}

	private struct function callApp( required string locale, string appParser = "", string tagParser = "" ) {
		var result = _InternalRequest(
			template: createURI( "index.cfm" ),
			url: { locale: arguments.locale, appParser: arguments.appParser, tagParser: arguments.tagParser }
		);
		return deserializeJSON( trim( result.filecontent ) );
	}

	private function restore() {
		if ( isNull( orgSysprop ) ) System.clearProperty( sysprop );
		else System.setProperty( sysprop, orgSysprop );
		if ( len( orgRaw ) ) cs.raw().dateParser = orgRaw;
		else structDelete( cs.raw(), "dateParser" );
		cs.resetDateParser();
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatepath() ), "\/" )#/";
		return baseURI & "LDEV7006/" & calledName;
	}
}
