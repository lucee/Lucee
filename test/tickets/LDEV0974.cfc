<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
		function run(){
			describe( title="Test cases for LDEV-974(Settings on Application.cfc)", body=function(){
				xit(title="Checking serializeJSON() with column wise serialization", body=function(){
					uri = createURI("LDEV0974/app1/index.cfm");
					local.result = _InternalRequest(template:uri);
					local.tmpData = deserializeJSON(local.result.FileContent.trim());
					expect(local.tmpData).toBeTypeOf("struct");
					expect(structKeyList(local.tmpData)).toBe("COLUMNS,DATA,ROWCOUNT");
				});

				it(title="Checking serializeJSON() with row wise serialization", body=function(){
					uri = createURI("LDEV0974/app2/index.cfm");
					local.result = _InternalRequest(template:uri);
					local.tmpData = deserializeJSON(local.result.FileContent.trim());
					expect(local.tmpData).toBeTypeOf("struct");
					expect(structKeyList(local.tmpData)).toBe("COLUMNS,DATA");
				});

				it(title="Checking serializeJSON() with struct serialization", body=function(){
					uri = createURI("LDEV0974/app3/index.cfm");
					local.result = _InternalRequest(template:uri);
					local.tmpData = deserializeJSON(local.result.FileContent.trim());
					expect(local.tmpData).toBeTypeOf("Array");
					expect(arrayLen(local.tmpData)).toBe("3");
				});

				xit(title="Checking serializeJSON() with preserve case for structkey(preservecaseforstructkey)", body=function(){
					uri = createURI("LDEV0974/app4/index.cfm");
					local.result = _InternalRequest(template:uri);
					local.tmpData = deserializeJSON(local.result.FileContent.trim());
					expect(find("Name", local.result.FileContent.trim())).toBeGT(0);
					expect(find("id", local.result.FileContent.trim())).toBeGT(0);
				});

				xit(title="Checking serializeJSON() with metadata for object to be serialized", body=function(){
					local.uri = createURI("LDEV0974/app5/index.cfm");
					local.result = _InternalRequest(template:local.uri);
					local.tmpData = deserializeJSON(local.result);
					expect(structKeyList(local.tmpData)).toBe("DESIGNATION,fname,id");
					expect(structKeyList(local.tmpData.fname)).toBeTypeOf("string");
					expect(structKeyList(local.tmpData.id)).toBeTypeOf("string");

				});

				xit(title="Checking serializeJSON() with custom serializer", body=function(){
					uri = createURI("LDEV0974/app6/index.cfm");
					local.result = _InternalRequest(template:uri);
					expect(local.result.FileContent.trim()).toBe("SERIALISED");
				});
			});

			describe( title="Test cases for LDEV-974(Settings on serializeJSON function)", body=function(){
				xit(title="Checking serializeJSON() with column wise serialization", body=function(){
					local.myresult = DummyFunction();
					jsonObject = serializeJSON(local.myresult, "column");
					local.tmpData = deserializeJSON(jsonObject);
					expect(local.tmpData).toBeTypeOf("struct");
					expect(structKeyList(local.tmpData)).toBe("COLUMNS,DATA,ROWCOUNT");
				});

				it(title="Checking serializeJSON() with row wise serialization", body=function(){
					local.myresult = DummyFunction();
					jsonObject = serializeJSON(local.myresult, "row");
					local.tmpData = deserializeJSON(jsonObject);
					expect(local.tmpData).toBeTypeOf("struct");
					expect(structKeyList(local.tmpData)).toBe("COLUMNS,DATA");
				});

				it(title="Checking serializeJSON() with struct serialization", body=function(){
					local.myresult = DummyFunction();
					jsonObject = serializeJSON(local.myresult, "struct");
					local.tmpData = deserializeJSON(jsonObject);
					expect(local.tmpData).toBeTypeOf("Array");
					expect(arrayLen(local.tmpData)).toBe("3");
				});

				xit(title="Checking serializeJSON() with preserve case for structkey(preservecaseforstructkey)", body=function(){
					// Skipped this as we can't use preserve case with serializeJSON()
					myStruct = structNew();
					mystruct.id = 1;
					mystruct.Name = "POTHYS";
					mystruct.DESIGNATION = "Associate Software Engineer";

					local.result = serializeJSON(myStruct);
					local.tmpData = deserializeJSON(local.result);
					expect(find("Name", local.result)).toBeGT(0);
					expect(find("id", local.result)).toBeGT(0);
				});

				xit(title="Checking serializeJSON() with metadata for object to be serialized", body=function(){
					myStruct = structNew();
					mystruct.id = 1;
					mystruct.Name = "POTHYS";
					mystruct.DESIGNATION = "Associate Software Engineer";
					metadata = {id: {type:"string"}, name: {type:"string",name:"fname"}};
					mystruct.setMetadata(metadata);

					local.result = serializeJSON(myStruct);
					local.tmpData = deserializeJSON(local.result);

					expect(local.tmpData).toBeTypeOf("struct");
					expect(structKeyList(local.tmpData)).toBe("DESIGNATION,fname,id");
					expect(structKeyList(local.tmpData.fname)).toBeTypeOf("string");
					expect(structKeyList(local.tmpData.id)).toBeTypeOf("string");
				});

				// it(title="Checking serializeJSON() with custom serializer", body=function(){
				// 	myStruct = structNew();
				// 	mystruct.id = 1;
				// 	mystruct.Name = "POTHYS";
				// 	mystruct.DESIGNATION = "Associate Software Engineer";

				// 	local.result = serializeJSON(myStruct, false, false, "LDEV0974.App6.custom.serialize");
				// 	local.tmpData = deserializeJSON(local.result);
				// 	expect(local.result.FileContent.trim()).toBe("SERIALISED");
				// });
			});
		}

		private string function createURI(string calledName){
			var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
			return baseURI & "" & calledName;
		}
	</cfscript>

	<cffunction name="DummyFunction" access="private">
		<cfset query = queryNew("ID, DateJoined", "INT, TIMESTAMP", [{ID=1, DateJoined="2017-01-03 10:57:54"}, {ID=2, DateJoined="2017-01-03 10:57:54"}, {ID=3, DateJoined="2017-01-03 10:57:54"}])>
		<cfreturn query>
	</cffunction>
</cfcomponent>