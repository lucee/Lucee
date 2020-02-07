<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
		function beforeAll(){
			afterAll();
			variables.uri = createURI("LDEV1941");
			Directorycreate(variables.uri);
		}
		function afterAll(){
			variables.uri = createURI("LDEV1941");
			if(directoryExists(variables.uri)){
				DirectoryDelete(variables.uri, true);
			}
		}
		
		function run( testResults , testBox ) {
			describe( "checking PDF action extracttext", function() {
				it("Checking on tagBased", function( currentSpec ) {
					var result = tagBased();
					expect(isXML(result)).toBeTrue();
				});

				it("checking script based", function( currentSpec ) {
					cfdocument(format="PDF" name="pdf2" filename="#variables.uri#/test2.pdf" overwrite="true"){
						writeOutput("Lucee");
					}
					cfpdf ( action = "extracttext" source = "pdf2" name="result" ) {
					}
					expect(isXML(result)).toBeTrue();
				});
			});
		}

		// private function//
		private string function createURI(string calledName){
			var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
			return baseURI&""&calledName;
		}
	</cfscript>

	<cffunction name="tagBased" access="private" returntype="any">
		<cfset path ="#getDirectoryFromPath(getCurrenttemplatepath())#"/>
		<cfdocument format="PDF" overwrite="true" name="pdf" filename="#variables.uri#/test.pdf">
			<cfdocumentsection>
				Lucee test documents
			</cfdocumentsection>
		</cfdocument>
		<cfpdf action = "extracttext" source = "pdf" name="read" >
		<cfreturn read>
	</cffunction>
</cfcomponent>