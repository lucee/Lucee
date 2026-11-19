component extends="org.lucee.cfml.test.LuceeTestCase" labels="systemutil" {

	function run( testResults, testBox ) {

		describe( "LDEV-6955 process temp directory", function() {

			it( "is created below java.io.tmpdir and named for this pid", function() {
				var systemUtil = createObject( "java", "lucee.commons.io.SystemUtil" );
				var processHandle = createObject( "java", "java.lang.ProcessHandle" );
				var procDir = systemUtil.getProcessTempDirectory();

				expect( procDir.exists() ).toBeTrue();

				var tmpRoot = createObject( "java", "java.io.File" ).init( createObject( "java", "java.lang.System" ).getProperty( "java.io.tmpdir" ) ).getCanonicalPath();
				var procFile = createObject( "java", "java.io.File" ).init( procDir.getPath() );
				var procPath = procFile.getCanonicalPath();

				expect( procPath ).toStartWith( tmpRoot );
				// the directory is a direct child of the temp root, nothing nested
				expect( procFile.getParent() ).toBe( tmpRoot );

				// the JDK appends a unique suffix after the pid, so containers sharing a pid can never collide
				var prefix = "lucee-pid-" & processHandle.current().pid().toString() & "-";
				var name = procFile.getName();
				expect( name ).toMatch( "^lucee-pid-\d+-\d+$" );
				expect( left( name, len( prefix ) ) ).toBe( prefix );
				expect( len( name ) ).toBeGT( len( prefix ) );
			});

			it( "returns the same directory on every call", function() {
				var systemUtil = createObject( "java", "lucee.commons.io.SystemUtil" );
				var marker = systemUtil.getProcessTempDirectory().getRealResource( "marker.txt" );
				fileTouch( marker.getPath() );
				expect( systemUtil.getProcessTempDirectory() ).toBeSameInstanceAs( systemUtil.getProcessTempDirectory() );
				expect( marker.exists() ).toBeTrue();
				marker.delete();
			});

			it( "is separate from the instance temp directory", function() {
				var systemUtil = createObject( "java", "lucee.commons.io.SystemUtil" );
				expect( systemUtil.getProcessTempDirectory().getPath() ).notToBe( systemUtil.getTempDirectory().getPath() );
			});

			it( "stages getTempFile() below the process temp directory", function() {
				var systemUtil = createObject( "java", "lucee.commons.io.SystemUtil" );
				var procPath = createObject( "java", "java.io.File" ).init( systemUtil.getProcessTempDirectory().getPath() ).getCanonicalPath();
				var file = systemUtil.getTempFile( "tmp", true );

				expect( file.exists() ).toBeTrue();
				expect( createObject( "java", "java.io.File" ).init( file.getPath() ).getCanonicalPath() ).toStartWith( procPath );

				file.delete();
			});
		});
	}
}
