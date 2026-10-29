component extends="org.lucee.cfml.test.LuceeTestCase" labels="callStackGet,dump" {

	function beforeAll() {
		variables.originalMappings = getApplicationSettings().mappings;
		variables.baseDir = getTempDirectory() & "LDEV3697-" & createUUID() & "/";
		variables.mapDir = variables.baseDir & "mapped/";
		variables.archiveSrc = variables.baseDir & "archiveSrc/";
		variables.larFile = variables.baseDir & "archive.lar";
		variables.archiveVirtual = "/ldev3697archive";
		directoryCreate( variables.mapDir, true, true );
		directoryCreate( variables.archiveSrc, true, true );

		var code = '<cfset request.ldev3697 = { stack: callStackGet( "array" ), context: getCurrentContext() }>';
		fileWrite( variables.mapDir & "inc.cfm", code );
		fileWrite( variables.archiveSrc & "inc.cfm", code );
	}

	function afterAll() {
		application action="update" mappings=variables.originalMappings;
		if ( directoryExists( variables.baseDir ) ) directoryDelete( variables.baseDir, true );
	}

	function run( testResults, testBox ) {

		describe( "LDEV-3697: callStackGet and getCurrentContext template paths", function() {

			it( "returns the absolute path of a normal template", function() {
				var stack = callStackGet( "array" ); var context = getCurrentContext();
				expect( stack[ 1 ].template ).toBe( getCurrentTemplatePath() );
				expect( context[ 1 ].template ).toBe( getCurrentTemplatePath() );
			} );

			it( "returns the same templates and lines from callStackGet and getCurrentContext", function() {
				var stack = callStackGet( "array" ); var context = getCurrentContext();
				expect( arrayLen( context ) ).toBe( arrayLen( stack ) );
				loop from=1 to=arrayLen( stack ) index="local.i" {
					expect( context[ i ].template ).toBe( stack[ i ].template );
					expect( context[ i ].line ).toBe( stack[ i ].lineNumber );
				}
			} );

			it( "returns the absolute path of a template included through a mapping", function() {
				var mappings = duplicate( variables.originalMappings );
				mappings[ "/ldev3697mapped" ] = variables.mapDir;
				application action="update" mappings=mappings;

				include "/ldev3697mapped/inc.cfm";

				var expected = getCanonicalPath( variables.mapDir & "inc.cfm" );
				expect( getCanonicalPath( request.ldev3697.stack[ 1 ].template ) ).toBe( expected );
				expect( getCanonicalPath( request.ldev3697.context[ 1 ].template ) ).toBe( expected );
				expect( request.ldev3697.stack[ 1 ].template ).toBe( request.ldev3697.context[ 1 ].template );
				expect( request.ldev3697.stack[ 2 ].template ).toBe( getCurrentTemplatePath() );
				expect( request.ldev3697.context[ 2 ].template ).toBe( getCurrentTemplatePath() );
			} );

			it( title="returns the template of a template included through an archive mapping", skip=!structKeyExists( request, "WEBADMINPASSWORD" ), body=function() {
				admin action="updateMapping" type="web" password="#request.WEBADMINPASSWORD#"
					virtual="#variables.archiveVirtual#" physical="#variables.archiveSrc#" archive="" primary="physical"
					toplevel="true" trusted="no";
				admin action="createArchive" type="web" password="#request.WEBADMINPASSWORD#"
					file="#variables.larFile#" virtual="#variables.archiveVirtual#" addCFMLFiles="false" addNonCFMLFiles="false";
				admin action="removeMapping" type="web" password="#request.WEBADMINPASSWORD#" virtual="#variables.archiveVirtual#";
				expect( fileExists( variables.larFile ) ).toBeTrue();
				// the source must be gone so the template can only come from the archive
				directoryDelete( variables.archiveSrc, true );

				var mappings = duplicate( variables.originalMappings );
				mappings[ variables.archiveVirtual ] = { archive: variables.larFile, primary: "archive" };
				application action="update" mappings=mappings;

				include "/ldev3697archive/inc.cfm";

				var first = request.ldev3697.stack[ 1 ].template;
				expect( listLast( replace( first, "\", "/", "all" ), "/" ) ).toBe( "inc.cfm" );
				expect( first ).toBe( request.ldev3697.context[ 1 ].template );
				expect( request.ldev3697.stack[ 2 ].template ).toBe( getCurrentTemplatePath() );
			} );

			it( "returns only the requested frames for offset and maxFrames", function() {
				// all calls are on the same line so the line of the first frame is the same
				var all = callStackGet( "array" ); var first = callStackGet( "array", 0, 1 ); var second = callStackGet( "array", 1, 1 ); var middle = callStackGet( "array", 2, 2 ); var rest = callStackGet( "array", 2 );
				expect( arrayLen( all ) ).toBeGTE( 4 );

				expect( first ).toBe( [ all[ 1 ] ] );
				expect( second ).toBe( [ all[ 2 ] ] );
				expect( middle ).toBe( [ all[ 3 ], all[ 4 ] ] );
				expect( rest ).toBe( arraySlice( all, 3 ) );
			} );

			it( "returns the available frames when maxFrames is larger than the stack", function() {
				var all = callStackGet( "array" ); var size = arrayLen( all ); var many = callStackGet( "array", 0, size + 50 ); var manyFromSecond = callStackGet( "array", 1, size + 50 ); var beyond = callStackGet( "array", size + 5, 1 );
				expect( many ).toBe( all );
				expect( manyFromSecond ).toBe( arraySlice( all, 2 ) );
				expect( beyond ).toBe( [] );
			} );

		} );

		describe( "LDEV-3697: cfdump shows the right template", function() {

			it( "shows the calling template as context by default", function() {
				var result = dumpContext();
				expect( titlePath( result.title ) ).toBe( getCurrentTemplatePath() );
				expect( titleLine( result.title ) ).toBeNumeric();
			} );

			it( "honors contextLevel", function() {
				expect( dumpContext( 0 ).title ).toBe( "[unknown file]:[unknown line]" );
				expect( titlePath( dumpContext( 1 ).title ) ).toBe( "/Dump.cfc" );

				var result = dumpContext( 2 );
				expect( titlePath( result.title ) ).toBe( result.stack[ 1 ].template );

				result = dumpContext( 3 );
				expect( result.title ).toBe( result.stack[ 2 ].template & ":" & result.stack[ 2 ].lineNumber );
			} );

			it( "uses the last frame if contextLevel is deeper than the stack", function() {
				var depth = arrayLen( callStackGet( "array" ) );
				var result = dumpContext( depth + 10 );
				var last = result.stack[ arrayLen( result.stack ) ];
				expect( result.title ).toBe( last.template & ":" & last.lineNumber );
			} );

		} );
	}

	private string function titlePath( required string title ) {
		return reReplace( arguments.title, ":\d+$", "" );
	}

	private string function titleLine( required string title ) {
		return reReplace( arguments.title, "^.*:(\d+)$", "\1" );
	}

	// dumps with the given contextLevel (default level if not provided), returns the title holding the context
	// and the stack as seen from here, the context level 2 is the first frame of that stack
	private struct function dumpContext( numeric contextLevel ) {
		var stack = callStackGet( "array" );
		var output = "";
		if ( structKeyExists( arguments, "contextLevel" ) ) {
			savecontent variable="output" {
				cfdump( var=1, format="html", contextLevel=arguments.contextLevel );
			}
		}
		else {
			savecontent variable="output" {
				cfdump( var=1, format="html" );
			}
		}
		var match = reFind( 'title="([^"]*)"', output, 1, true );
		expect( match.pos[ 1 ] ).toBeGT( 0 );
		return { title: mid( output, match.pos[ 2 ], match.len[ 2 ] ), stack: stack };
	}

}
