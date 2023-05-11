<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase"  labels="pdf">
	<cfscript>
		function beforeAll(){
			uri = createURI("LDEV0499");
			if(not directoryExists(uri)){
				Directorycreate(uri);
			}
		}
		
		function run( testResults , testBox ) {
			describe( "Test suite for LDEV-499", function() {
				it("Checking CFdocument with encryption, without attribute userpassword", function( currentSpec ) {
					var result = CFdocumentWithoutAttribute();
					// expect(result).toBeTypeOf('Struct');
					expect(result.Encryption).toBe('Password Security');
				});

				it("Checking CFdocument with encryption, with attribute userpassword='' while creating pdf", function( currentSpec ) {
					var result = CFdocumentWithEncryption();
					expect(result.Encryption).toBe('Password Security');
				});

				it("Checking CFdocument with encryption, with attribute password='' while reading pdf", function( currentSpec ) {
					var result = CFdocumentWitheEmptyValueUserPassword();
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
		<cfset path ="#getDirectoryFromPath(getCurrenttemplatepath())#"/>
		<cfdocument format="PDF" overwrite="true" encryption="40-bit" filename="#path#LDEV0499/test.pdf">
			<cfdocumentsection>
				Lucee test documents
			</cfdocumentsection>
		</cfdocument>
		<cfpdf action="read" name="pdfDetails" source="#path#LDEV0499/test.pdf"/>
		<cfreturn pdfDetails>
	</cffunction>

	<cffunction name="CFdocumentWithEncryption" access="private" returntype="Struct">
		<cfset path ="#getDirectoryFromPath(getCurrenttemplatepath())#"/>
		<cfdocument format="PDF"  overwrite="true" userpassword="" encryption="40-bit"  filename="#path#LDEV0499/test.pdf">
			<cfdocumentsection>
				Lucee test documents
			</cfdocumentsection>
		</cfdocument>
		<cfpdf action="read" name="pdfDetails" source="#path#LDEV0499/test.pdf"/>
		<cfreturn  pdfDetails>
	</cffunction>

	<cffunction name="CFdocumentWitheEmptyValueUserPassword" access="private" returntype="Struct">
		<cfset path ="#getDirectoryFromPath(getCurrenttemplatepath())#"/>
		<cfdocument format="PDF" overwrite="true" encryption="40-bit" filename="#path#LDEV0499/test.pdf">
			<cfdocumentsection>
				Lucee test documents
			</cfdocumentsection>
		</cfdocument>
		<cfpdf action="read" name="pdfDetails" source="#path#LDEV0499/test.pdf" password=""/>
		<cfreturn pdfDetails>
	</cffunction>
</cfcomponent>