component extends=org.lucee.cfml.test.LuceeTestCase skip=true {

	function beforeAll() {
		variables.testPdf = getTempFile( getTempDirectory(), "ldev4694", "pdf" );
		variables.password = createUniqueID();

		if( not fileExists('#testPdf#') ){
			document format="pdf" filename="#testPdf#" overwrite=true {
				echo("   ");
			}
			pdf action="protect" source="#testPdf#" newUserPassword="#password#";
		}
	}

	function run( testResults , testBox ) {
		describe( title="Testcase for LDEV-4694" , body=function() {
			it( title="Checking for invalid password with action='removepassword' in cfpdf" , body=function( currentSpec ) {
				expect( function(){
					pdf action="removePassword" source=#testPdf# destination=#testPdf# password="invalidPassword" overwrite=true;
				}).toThrow();
				expect( fileExists( variables.testPdf ) ).toBeFalse();
			});
		});
	}

	function afterAll(){
		if ( fileExists( variables.testPdf ) )
			fileDelete( variables.testPdf );
	};
}
