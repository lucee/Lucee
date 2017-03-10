<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
		function run(){
			describe( title="Test cases for LDEV-1173", body=function(){
				it(title="Checking serializeJSON() with useSecureJSONPrefix", body=function(){
					uri = createURI("LDEV1173/app1/index.cfm");
					local.result = _InternalRequest(template:uri);
					expect(local.result.FileContent.trim()).toBe('//{"ROWCOUNT":2,"COLUMNS":["ID","DATEJOINED"],"DATA":{"ID":[1,2],"DATEJOINED":["January, 30 2017 10:57:54","January, 30 2017 10:57:54"]}}');
				});

				it(title="Checking serializeJSON() with custom serializer", body=function(){
					uri = createURI("LDEV1173/app2/index.cfm");
					local.result = _InternalRequest(template:uri);
					expect(local.result.FileContent.trim()).toBe("//SERIALISED");
				});
			});
		}

		private string function createURI(string calledName){
			var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
			return baseURI & "" & calledName;
		}
	</cfscript>
</cfcomponent>