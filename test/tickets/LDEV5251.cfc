component extends="org.lucee.cfml.test.LuceeTestCase" {

	variables.FILES_PER_DIR = 12;
	variables.SUBDIRS = [ "", "sub1", "sub2", "sub2/deep" ];
	variables.BROKEN = [ "broken-b.cfm", "sub1/broken-a.cfc", "sub2/deep/broken-c.cfm" ];

	function beforeAll() {
		variables.id = "LDEV5251" & replace( createGUID(), "-", "", "all" );
		variables.baseDir = getTempDirectory() & variables.id & "/";
		variables.created = [];
		variables.mappingDirs = {};
		variables.mappingNames = {};
		directoryCreate( variables.baseDir );
	}

	function afterAll() {
		for ( var virtual in variables.created ) {
			try {
				admin action="removeMapping" type="server" password="#request.SERVERADMINPASSWORD#" virtual=virtual;
			}
			catch ( any e ) {
				systemOutput( "failed to remove mapping #virtual#: #e.message#", true );
			}
		}
		if ( directoryExists( variables.baseDir ) ) directoryDelete( variables.baseDir, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-5251: admin action=compileMapping with threads", function() {

			it( title="threads=1 and threads>1 compile the same templates", body=function( currentSpec ) {
				var single = createMapping( "valid-single", false );
				var multi = createMapping( "valid-multi", false );

				compile( single, 1 );
				compile( multi, 4 );

				var singleClasses = compiledClasses( single );
				var multiClasses = compiledClasses( multi );

				expect( singleClasses ).notToBeEmpty();
				expect( multiClasses ).toBe( singleClasses );
				expect( arrayLen( multiClasses ) ).toBeGTE( 2 * variables.FILES_PER_DIR * arrayLen( variables.SUBDIRS ) );
			});

			it( title="default (no threads) is the same as threads=1", body=function( currentSpec ) {
				var withDefault = createMapping( "valid-default", false );
				var withOne = createMapping( "valid-one", false );

				admin action="compileMapping" type="server" password="#request.SERVERADMINPASSWORD#" virtual=withDefault;
				compile( withOne, 1 );

				expect( compiledClasses( withDefault ) ).toBe( compiledClasses( withOne ) );
			});

			it( title="errors are collected the same way with threads=1 and threads>1", body=function( currentSpec ) {
				var single = createMapping( "errors-single", true );
				var multi = createMapping( "errors-multi", true );

				var singleErrors = collectErrors( single, 1 );
				var multiErrors = collectErrors( multi, 4 );

				expect( structCount( singleErrors ) ).toBe( arrayLen( variables.BROKEN ) );
				expect( structCount( multiErrors ) ).toBe( arrayLen( variables.BROKEN ) );
				expect( multiErrors ).toBe( singleErrors );
				expect( structKeyList( multiErrors ) ).toBe( structKeyList( singleErrors ) );
				loop collection=#multiErrors# item="local.key" {
					expect( multiErrors[ key ] ).toHaveKey( "message" );
					expect( multiErrors[ key ] ).toHaveKey( "line" );
				}

				// the valid templates are compiled as well, a broken one does not stop the others
				expect( compiledClasses( multi ) ).toBe( compiledClasses( single ) );
				expect( arrayLen( compiledClasses( multi ) ) ).toBeGTE( 2 * variables.FILES_PER_DIR * arrayLen( variables.SUBDIRS ) );
			});

			it( title="stoponerror=true throws the same (first) error with threads=1 and threads>1", body=function( currentSpec ) {
				var single = createMapping( "stop-single", true );
				var multi = createMapping( "stop-multi", true );

				var singleMessage = stopOnErrorMessage( single, 1 );
				var multiMessage = stopOnErrorMessage( multi, 4 );

				expect( singleMessage ).notToBeEmpty();
				expect( normalize( multiMessage, multi ) ).toBe( normalize( singleMessage, single ) );
				expect( singleMessage ).toInclude( "broken" );
			});

			it( title="stoponerror=true without errors does not throw with threads>1", body=function( currentSpec ) {
				var multi = createMapping( "stop-valid", false );
				compile( multi, 4 );
				expect( arrayLen( compiledClasses( multi ) ) ).toBeGTE( 2 * variables.FILES_PER_DIR * arrayLen( variables.SUBDIRS ) );
			});

			it( title="ignoreScopes works with threads>1 and does not change the calling request", body=function( currentSpec ) {
				var multi = createMapping( "ignorescopes", false );
				admin action="compileMapping" type="server" password="#request.SERVERADMINPASSWORD#" virtual=multi ignoreScopes="true" threads="4";

				expect( arrayLen( compiledClasses( multi ) ) ).toBeGTE( 2 * variables.FILES_PER_DIR * arrayLen( variables.SUBDIRS ) );
				// scopes are still available in this request
				url.ldev5251 = "yes";
				expect( url.ldev5251 ).toBe( "yes" );
				expect( isStruct( form ) ).toBeTrue();
				expect( isDefined( "url.ldev5251" ) ).toBeTrue();
			});

			it( title="threads larger than the number of processors is accepted", body=function( currentSpec ) {
				var multi = createMapping( "many", false );
				compile( multi, 1000 );
				expect( arrayLen( compiledClasses( multi ) ) ).toBeGTE( 2 * variables.FILES_PER_DIR * arrayLen( variables.SUBDIRS ) );
			});

			it( title="invalid threads values are rejected", body=function( currentSpec ) {
				var multi = createMapping( "invalid", false );
				loop array=[ 0, -1, "abc" ] item="local.invalid" {
					expect( function() {
						compile( multi, invalid );
					}).toThrow( message="threads" );
				}
			});

			it( title="many templates in one directory compile with threads>1", body=function( currentSpec ) {
				var multi = createMapping( "wide", false, 40 );
				compile( multi, 8 );
				expect( arrayLen( compiledClasses( multi ) ) ).toBeGTE( 2 * 40 * arrayLen( variables.SUBDIRS ) );
			});

		});
	}

	private string function createMapping( required string name, required boolean withBroken, numeric filesPerDir=variables.FILES_PER_DIR ) {
		var virtual = "/#variables.id#-#arguments.name#";
		var dir = variables.baseDir & arguments.name & "/";
		directoryCreate( dir );

		loop array=#variables.SUBDIRS# item="local.sub" {
			var target = dir & sub;
			if ( len( sub ) && !directoryExists( target ) ) directoryCreate( target, true );
			loop from=1 to=arguments.filesPerDir index="local.i" {
				var nbr = numberFormat( i, "000" );
				var prefix = replace( sub, "/", "-", "all" );
				fileWrite( target & "/page-#prefix#-#nbr#.cfm", validTemplate( i ) );
				fileWrite( target & "/Comp#nbr#.cfc", validComponent( i ) );
			}
		}

		if ( arguments.withBroken ) {
			loop array=#variables.BROKEN# item="local.broken" {
				if ( right( broken, 4 ) == ".cfc" ) {
					fileWrite( dir & broken, 'component {' & chr( 10 ) & '	function broken( {' & chr( 10 ) & '}' );
				}
				else {
					fileWrite( dir & broken, '<cfset x = 1>' & chr( 10 ) & '<cfif x eq 1>' & chr( 10 ) & '<cfoutput>##x##</cfoutput>' );
				}
			}
		}

		admin action="updateMapping" type="server" password="#request.SERVERADMINPASSWORD#"
			virtual=virtual
			physical=dir
			toplevel="true"
			archive=""
			primary="physical"
			trusted="no";

		arrayAppend( variables.created, virtual );
		variables.mappingDirs[ virtual ] = dir;
		variables.mappingNames[ virtual ] = arguments.name;
		return virtual;
	}

	private string function validTemplate( required numeric i ) {
		return '<cfset total = 0>' & chr( 10 )
			& '<cfloop from="1" to="#arguments.i#" index="n"><cfset total += n></cfloop>' & chr( 10 )
			& '<cfoutput>##total##</cfoutput>';
	}

	private string function validComponent( required numeric i ) {
		return 'component {' & chr( 10 )
			& '	function value() {' & chr( 10 )
			& '		var total = 0;' & chr( 10 )
			& '		for ( var n = 1; n <= #arguments.i#; n++ ) total += n;' & chr( 10 )
			& '		return total;' & chr( 10 )
			& '	}' & chr( 10 )
			& '}';
	}

	private void function compile( required string virtual, required any threads ) {
		admin action="compileMapping" type="server" password="#request.SERVERADMINPASSWORD#" virtual=arguments.virtual threads=arguments.threads;
	}

	private struct function collectErrors( required string virtual, required numeric threads ) {
		var compileErrors = {};
		admin action="compileMapping" type="server" password="#request.SERVERADMINPASSWORD#"
			virtual=arguments.virtual
			stoponerror="false"
			errorVariable="compileErrors"
			threads=arguments.threads;
		return normalizeErrors( compileErrors, arguments.virtual );
	}

	private string function stopOnErrorMessage( required string virtual, required numeric threads ) {
		try {
			admin action="compileMapping" type="server" password="#request.SERVERADMINPASSWORD#"
				virtual=arguments.virtual
				stoponerror="true"
				threads=arguments.threads;
		}
		catch ( any e ) {
			return e.message;
		}
		return "";
	}

	private string function normalize( required string text, required string virtual ) {
		return replace( replace( arguments.text, variables.mappingDirs[ arguments.virtual ], "", "all" ), variables.mappingDirs[ arguments.virtual ].replace( "\", "/" ), "", "all" );
	}

	private struct function normalizeErrors( required struct errors, required string virtual ) {
		var result = structNew( "linked" );
		loop collection=#arguments.errors# item="local.key" {
			var info = structNew( "linked" );
			loop collection=#arguments.errors[ key ]# item="local.field" {
				var value = arguments.errors[ key ][ field ];
				info[ field ] = isSimpleValue( value ) ? normalize( value, arguments.virtual ) : value;
			}
			result[ normalize( key, arguments.virtual ) ] = info;
		}
		return result;
	}

	private array function compiledClasses( required string virtual ) {
		var marker = variables.id & "_" & replace( variables.mappingNames[ arguments.virtual ], "-", "_", "all" );
		var classesDir = expandPath( "{lucee-server}/cfclasses" );
		var result = [];
		loop array=#directoryList( classesDir, true, "path", "*.class" )# item="local.classFile" {
			var normalized = replace( classFile, "\", "/", "all" );
			var pos = findNoCase( marker, normalized );
			if ( pos == 0 ) continue;
			var rest = mid( normalized, pos + len( marker ) );
			if ( !reFind( "^[0-9]+/", rest ) ) continue;
			arrayAppend( result, reReplace( rest, "^[0-9]+/", "" ) );
		}
		arraySort( result, "text" );
		return result;
	}

}
