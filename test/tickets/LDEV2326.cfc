component extends = "org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function beforeAll(){

		variables.tempDir=getTempDirectory() & "LDEV2326_" & createGUID();
		afterAll();
		directoryCreate( variables.tempDir );
		cfloop( from = "1" to = "2" index = "i" ){
			cfdocument(format = "PDF" filename = "#variables.tempDir#/#i#.pdf" overwrite = "true"){
				writeOutput("lucee");
			}
		}
	}

	function afterAll(){
		if ( directoryExists( variables.tempDir ) ){
			directoryDelete( variables.tempDir, true );
		}
	}

	function run( testResults , testBox ) {
		describe( "test case for LDEV-2326", function() {
			it(title = "PDF action = merge with overwrite = false", body = function( currentSpec ) {
				var dest = getTempFile(getTempDirectory(), "LDEV2326", "pdf");
				expect(function(){
					cfpdf( action = 'merge', directory=variables.tempDir, destination=dest, overwrite=false );
				}).toThrow();

				fileDelete( dest );
				cfpdf( action = 'merge', directory=variables.tempDir, destination=dest, overwrite=false );
				
				expect( fileExists( dest ) ).toBe( true );
				expect( isPdfObject( dest ) ).toBe( true );
			});

			it(title = "PDF action=merge with overwrite = true", body = function( currentSpec ) {
				var dest = getTempFile(getTempDirectory(), "LDEV2326", "pdf");
				cfpdf( action = 'merge', directory=variables.tempDir, destination=dest, overwrite=true );
				expect( fileExists( dest ) ).toBe( true );
				expect( isPdfObject( dest ) ).toBe( true );
			});
		});
	}

}