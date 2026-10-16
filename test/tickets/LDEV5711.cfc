component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf" {

	function run( testResults , testBox ) {
		describe( title="LDEV-5711 cfdocumentsection", body=function() {

			it(title="cfdocument with cfdocumentSection with marginTop", body = function( currentSpec ) {
				var tmpFile = getTempFile("", "ldev-5711", "pdf");
				cfDocument(	overwrite=true,
						filename = "#tmpFile#",
						margintop = "1"   // remove this line, no exception
					) {
					cfdocumentsection(
						margintop = "0", // remove this line, no exception
						name = "pageName" ) {
						echo("<html><body><h1>LDEV-5711</h1></body></html>");
					}
				};
				expect( isPDFObject( tmpFile ) ).toBeTrue();
			});

			it(title="cfdocument with cfdocumentSection without marginTop", body = function( currentSpec ) {
				var tmpFile = getTempFile("", "ldev-5711", "pdf");
				cfDocument(	overwrite=true,	filename = "#tmpFile#" ) {
					cfdocumentsection(
						name = "pageName" ) {
						echo("<html><body><h1>LDEV-5711</h1></body></html>");
					}
				};
				expect( isPDFObject( tmpFile ) ).toBeTrue();
			});

			it(title="just cfdocument", body = function( currentSpec ) {
				var tmpFile = getTempFile("", "ldev-5711", "pdf");
				cfDocument( filename = "#tmpFile#", overwrite=true ) {
						echo("<html><body><h1>LDEV-5711</h1></body></html>");
				};
				expect( isPDFObject(tmpFile) ).toBeTrue();
			});

		});
	}
}