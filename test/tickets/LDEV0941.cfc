<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase"  labels="pdf,zip">
	<cfscript>
		
		function run( testResults , testBox ) {
			describe( "Test suite for LDEV-941", function() {
				it("checking cfzip tag, with empty folder inside the source folder", function( currentSpec ) {
					var result = zipfunctiononFolder();
					expect(find('testFolder/', result) > 0).toBeTrue();
				});
			});
		}
		
		private function zipfunctiononFolder() localmode=true {
			path = server._getTempDir( "LDEV0941" );

			if (!fileExists('#path#/test.pdf')){
				cfdocument(format="PDF" filename='#path#/test.pdf'){
					echo("<h1>LDEV-0941 test</h1>");
				}
			}

			if (!directoryExists('#path#/testFolder')){
				directorycreate("#path#/testFolder");
			}

			zip action="zip" source="#path#" file="#path#/test.zip";
			zip action="list" name="record" file="#path#/test.zip" showDirectory="true";
			serializedQry =  serializeJSON(record,true);
			fileDelete("#path#/test.zip");
			return serializedQry;
		}
	</cfscript>
</cfcomponent>