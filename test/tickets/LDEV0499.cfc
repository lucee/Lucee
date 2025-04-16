<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase"  labels="pdf">
	<cfscript>
		function run( testResults , testBox ) {
			describe( "Test suite for LDEV-499", function() {
				it("Checking CFdocument with encryption, without attribute userpassword", function( currentSpec ) {
					var result = CFdocumentWithoutAttribute();
					// expect(result).toBeTypeOf('Struct');
					expect(result.Encryption).toBe('No Security');
				});

				it("Checking CFdocument with encryption, with attribute userpassword='' while creating pdf", function( currentSpec ) {
					var result = CFdocumentWithEncryption();
					expect(result.Encryption).toBe('No Security');
				});

				it("Checking CFdocument with encryption, with attribute password='' while reading pdf", function( currentSpec ) {
					var result = CFdocumentWitheEmptyValueUserPassword();
					expect(result.Encryption).toBe('No Security');
				});

				it("Checking CFdocument with encryption, with password", function( currentSpec ) {
					var result = CFdocumentWithPassword();
					expect(result.Encryption).toBe('Password Security');
				});

			});
		}
		// private function//
		private string function createURI(string calledName){
			var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
			return baseURI&""&calledName;
		}
	</cfscript>

	<cffunction name="CFdocumentWithoutAttribute" access="private" returntype="Struct">
		<cfset var testPDF=getTempFile(getTempDirectory(), "LDEV0499", "pdf")>
		<cfdocument format="PDF" overwrite="true" encryption="40-bit" filename="#testPDF#">
			<cfdocumentsection>
				Lucee test documents
			</cfdocumentsection>
		</cfdocument>
		<cfpdf action="read" name="local.pdfDetails" source="#testPDF#"/>
		<cfreturn pdfDetails>
	</cffunction>

	<cffunction name="CFdocumentWithEncryption" access="private" returntype="Struct">
		<cfset var testPDF=getTempFile(getTempDirectory(), "LDEV0499", "pdf")>
		<cfdocument format="PDF"  overwrite="true" userpassword="" encryption="40-bit"  filename="#testPDF#">
			<cfdocumentsection>
				Lucee test documents
			</cfdocumentsection>
		</cfdocument>
		<cfpdf action="read" name="local.pdfDetails" source="#testPDF#"/>
		<cfreturn  pdfDetails>
	</cffunction>

	<cffunction name="CFdocumentWitheEmptyValueUserPassword" access="private" returntype="Struct">
		<cfset var testPDF=getTempFile(getTempDirectory(), "LDEV0499", "pdf")>
		<cfdocument format="PDF" overwrite="true" encryption="40-bit" filename="#testPDF#">
			<cfdocumentsection>
				Lucee test documents
			</cfdocumentsection>
		</cfdocument>
		<cfpdf action="read" name="local.pdfDetails" source="#testPDF#" password=""/>
		<cfreturn pdfDetails>
	</cffunction>

	<cffunction name="CFdocumentWithPassword" access="private" returntype="Struct">
		<cfset var testPDF=getTempFile(getTempDirectory(), "LDEV0499", "pdf")>
		<cfdocument format="PDF" overwrite="true" encryption="40-bit" filename="#testPDF#" userpassword="Lucee">
			<cfdocumentsection>
				Lucee test documents
			</cfdocumentsection>
		</cfdocument>
		<cfpdf action="read" name="local.pdfDetails" source="#testPDF#" password="Lucee"/>
		<cfreturn pdfDetails>
	</cffunction>
</cfcomponent>