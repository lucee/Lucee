<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
		function beforeAll(){
			uri = createURI("testFolder");
            afterAll();
			if(not directoryExists(uri)){
				Directorycreate(uri);
			}

			if(not fileExists('#uri#/test.pdf')){
				cfdocument(format="PDF" filename='#uri#/test.pdf'){
				}
			}

			if(not directoryExists('#uri#/testFolder')){
				Directorycreate("#uri#/testFolder");
			}
		}

		function run( testResults , testBox ) {
			describe( "Test suite for LDEV-941", function() {
				it("checking cfzip tag, with empty folder inside the source folder", function( currentSpec ) {
					var result = zipfunctiononFolder();
					expect(find('testFolder/', result) > 0).toBeTrue();
				});
			});
		}
		// private function//
		private string function createURI(string calledName){
			var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
			return baseURI&""&calledName;
		}

		function afterAll(){
			if(directoryExists(uri)){
				directoryDelete(uri,true);
			}
		}
	</cfscript>

	<cffunction name="zipfunctiononFolder" access="private" returntype="Any">
		<cfset path ="#getDirectoryFromPath(getCurrenttemplatepath())#" />
		<cfzip action="zip" source="#path#testFolder" file="#path#test.zip">
		<cfzip action="list" name="record" file="#path#test.zip" showDirectory="true">
		<cfset serializedqry =  serializeJSON(record,true)>
		<cfreturn serializedqry>
	</cffunction>
</cfcomponent>