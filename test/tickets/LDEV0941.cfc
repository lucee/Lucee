<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
		function beforeAll(){
			variables.path = getDirectoryFromPath(getCurrenttemplatepath()) & "LDEV0941";
			variables.uri = path & "/testFolder";
	
			afterAll();
			
			if(!directoryExists(path)){
				Directorycreate(path);
			}

			if(!directoryExists(uri)){
				Directorycreate(uri);
			}

			if(!fileExists('#uri#/test.pdf')){
				cfdocument(format="PDF" filename='#uri#/test.pdf'){
				}
			}

			if(!directoryExists('#uri#/testFolder')){
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

		function afterAll(){
			if(directoryExists(path)){
				directoryDelete(path,true);
			}
		}
	</cfscript>

	<cffunction name="zipfunctiononFolder" access="private" returntype="Any">
		<cfzip action="zip" source="#uri#" file="#path#/test.zip">
		<cfzip action="list" name="record" file="#path#/test.zip" showDirectory="true">
		<cfset serializedqry =  serializeJSON(record,true)>
		<cfreturn serializedqry>
	</cffunction>
</cfcomponent>