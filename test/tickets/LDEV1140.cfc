component extends="org.lucee.cfml.test.LuceeTestCase" labels="compress" {

	function beforeAll() {
		variables.fixtures = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV1140/";
		variables.dir = getTempDirectory() & "ldev1140-" & createUUID() & "/";
		directoryCreate( dir );
	}

	function afterAll() {
		if ( directoryExists( dir ) ) directoryDelete( dir, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-1140 unzip entry names", function() {

			it( "extracts a nested folder structure", function() {
				var out = dir & "nested-extract/";
				directoryCreate( out );
				extract( "zip", fixtures & "nested-utf8.zip", out );
				expect( fileRead( out & "photos/2024/a.txt" ) ).toBe( "hello nested" );

				if ( coreExtractsTar() ) {
					var outTar = dir & "nested-tar/";
					directoryCreate( outTar );
					extract( "tar", fixtures & "nested.tar", outTar );
					expect( fileRead( outTar & "photos/2024/a.txt" ) ).toBe( "hello nested" );
				}
			});

			it( "extracts a zip whose entry names are not UTF-8", function() {
				var out = dir & "cp437-extract/";
				directoryCreate( out );
				extract( "zip", fixtures & "cp437-kase.zip", out );
				expect( fileExists( out & "käse.txt" ) ).toBeTrue();
				expect( fileRead( out & "käse.txt" ) ).toBe( "LDEV-1140" );
			});

			it( "extracts a nested non-UTF-8 entry name", function() {
				var out = dir & "cp437-nested/";
				directoryCreate( out );
				extract( "zip", fixtures & "cp437-nested.zip", out );
				expect( fileRead( out & "photos/2024/käse.txt" ) ).toBe( "LDEV-1140" );
			});

			it( "rejects an entry name that resolves outside the target directory", function() {
				assertRejected( "zip", fixtures & "outside-parent.zip", "parent", "outside.txt" );
				if ( coreExtractsTar() ) {
					assertRejected( "tar", fixtures & "outside-parent.tar", "parent-tar", "outside.txt" );
				}
			});

			it( "rejects an absolute entry name", function() {
				assertRejected( "zip", fixtures & "outside-abs.zip", "abs", "abs.txt" );
				if ( coreExtractsTar() ) {
					assertRejected( "tar", fixtures & "outside-abs.tar", "abs-tar", "abs.txt" );
				}
			});

			it( "rejects an entry name that would resolve to a sibling of the target directory", function() {
				assertRejectedSibling( "zip", fixtures & "outside-sibling.zip", "sib" );
				if ( coreExtractsTar() ) {
					assertRejectedSibling( "tar", fixtures & "outside-sibling.tar", "sib-tar" );
				}
			});

		});
	}

	// core extract() still handles tar on 6.2 and 7.0; 7.1+ only does zip/gzip in core
	private boolean function coreExtractsTar() {
		var v = server.lucee.version;
		var maj = val( listFirst( v, "." ) );
		var min = val( listGetAt( v, 2, "." ) );
		return maj < 7 || ( maj == 7 && min == 0 );
	}

	private void function assertRejected( required string format, required string archive, required string label, required string outsideName ) {
		var work = dir & label & "/";
		var dest = work & "dest/";
		directoryCreate( dest );
		var failed = false;
		try {
			extract( format, archive, dest );
		}
		catch ( any e ) {
			failed = true;
			expect( e.message contains "outside the target directory" || e.detail contains "outside the target directory" || e.message contains "archive entry" )
				.toBeTrue( "unexpected error for #label#: #e.message# #e.detail#" );
		}
		expect( failed ).toBeTrue( "#label# should reject the entry name" );
		expect( fileExists( work & outsideName ) ).toBeFalse();
	}

	private void function assertRejectedSibling( required string format, required string archive, required string label ) {
		var work = dir & label & "/";
		var dest = work & "target/";
		var sibling = work & "target2/";
		directoryCreate( dest );
		directoryCreate( sibling );
		var failed = false;
		try {
			extract( format, archive, dest );
		}
		catch ( any e ) {
			failed = true;
			expect( e.message contains "outside the target directory" || e.detail contains "outside the target directory" || e.message contains "archive entry" )
				.toBeTrue( "unexpected error for #label#: #e.message# #e.detail#" );
		}
		expect( failed ).toBeTrue( "#label# should reject the sibling entry name" );
		expect( fileExists( sibling & "x.txt" ) ).toBeFalse();
		expect( directoryList( sibling, true ).len() ).toBe( 0 );
	}

}
