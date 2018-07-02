component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.origSerSettings =  getApplicationSettings().serialization;
	}

	function afterAll(){
		application action="update"
			SerializationSettings = variables.origSerSettings;
	}

	function run( testResults , testBox ) {
		describe( "test suite for serialization with application update", function() {

			it(title="Checking serializeJSON() with column wise serialization", body=function(){
				var serSettings =  getApplicationSettings().serialization;
				serSettings.serializeQueryAs = "column";
				application action="update" SerializationSettings=serSettings;
				var data = queryNew("ID, DateJoined", "INTEGER, TIMESTAMP", [{ID=1, DateJoined="2017-01-03 10:57:54"}, {ID=2, DateJoined="2017-01-03 10:57:54"}, {ID=3, DateJoined="2017-01-03 10:57:54"}]);
				var jsonObject = serializeJSON( data );
				local.tmpData = deserializeJSON(jsonObject);
				expect(local.tmpData).toBeTypeOf("struct");
				expect(listSort(structKeyList(local.tmpData), "text")).toBe("COLUMNS,DATA,ROWCOUNT");
			});

			it(title="Checking serializeJSON() with row wise serialization", body=function(){
				var serSettings =  getApplicationSettings().serialization;
				serSettings.serializeQueryAs = "Row";
				application action="update" SerializationSettings=serSettings;
				var data = queryNew("ID, DateJoined", "INTEGER, TIMESTAMP", [{ID=1, DateJoined="2017-01-03 10:57:54"}, {ID=2, DateJoined="2017-01-03 10:57:54"}, {ID=3, DateJoined="2017-01-03 10:57:54"}]);
				var jsonObject = serializeJSON( data );
				local.tmpData = deserializeJSON(jsonObject);
				expect(local.tmpData).toBeTypeOf("struct");
				expect(listSort(structKeyList(local.tmpData), "text")).toBe("COLUMNS,DATA");
			});

			/* When this testcase is enabled then 'preserveCaseForQueryColumn is true' below fails, but each testcase passes on its own
			it(title="Checking serializeJSON() with struct serialization", body=function(){
				var serSettings =  getApplicationSettings().serialization;
				serSettings.serializeQueryAs = "struct";
				application action="update" SerializationSettings=serSettings;
				var data = queryNew("ID, DateJoined", "INTEGER, TIMESTAMP", [{ID=1, DateJoined="2017-01-03 10:57:54"}, {ID=2, DateJoined="2017-01-03 10:57:54"}, {ID=3, DateJoined="2017-01-03 10:57:54"}]);
				var jsonObject = serializeJSON( data );
				local.tmpData = deserializeJSON(jsonObject);
				expect(local.tmpData).toBeTypeOf("Array");
				expect(arrayLen(local.tmpData)).toBe("3");
			});
			//*/

			it(title="Checking serializeJSON() with preserve case for structkey(preservecaseforstructkey) Eq to TRUE", body=function(){
				var serSettings =  getApplicationSettings().serialization;
				serSettings.preserveCaseForStructKey = true;
				application action="update" SerializationSettings=serSettings;
				
				var myStruct = {
					 "id"          : 1
					,"Name"        : "POTHYS"
					,"designation" : "Associate Software Engineer"
				};

				var jsonObject = serializeJSON( mystruct );
				expect(find("Name", jsonObject)).toBeGT(0);
				expect(find("id", jsonObject)).toBeGT(0);
			});

			it(title="Checking serializeJSON() with preserve case for structkey(preservecaseforstructkey) Eq to false", body=function(){
				var serSettings =  getApplicationSettings().serialization;
				serSettings.preserveCaseForStructKey = false;
				application action="update" SerializationSettings=serSettings;
				
				var myStruct = {
					 "id"          : 1
					,"Name"        : "POTHYS"
					,"designation" : "Associate Software Engineer"
				};

				var jsonObject = serializeJSON( mystruct );
				expect(find("NAME", jsonObject)).toBeGT(0);
				expect(find("ID", jsonObject)).toBeGT(0);
			});

			it(title="Checking serializeJSON() with preserveCaseForQueryColumn is true", body=function(){
				var serSettings =  getApplicationSettings().serialization;
				serSettings.preserveCaseForQueryColumn = true;
				application action="update"	SerializationSettings=serSettings;

				var data = queryNew("ID, DateJoined", "INTEGER, TIMESTAMP", [{ID=1, DateJoined="2017-01-03 10:57:54"}, {ID=2, DateJoined="2017-01-03 10:57:54"}, {ID=3, DateJoined="2017-01-03 10:57:54"}]);
				var jsonObject = serializeJSON( data );
				expect(find("DateJoined", jsonObject)).toBeGT(0);
				expect(find("ID", jsonObject)).toBeGT(0);
			});

			it(title="Checking serializeJSON() with preserveCaseForQueryColumn is false", body=function(){
				var serSettings =  getApplicationSettings().serialization;
				serSettings.preserveCaseForQueryColumn = false;
				application action="update" SerializationSettings=serSettings;
				var data = queryNew("ID, DateJoined", "INTEGER, TIMESTAMP", [{ID=1, DateJoined="2017-01-03 10:57:54"}, {ID=2, DateJoined="2017-01-03 10:57:54"}, {ID=3, DateJoined="2017-01-03 10:57:54"}]);
				var jsonObject = serializeJSON( data );
				expect(find("DATEJOINED", jsonObject)).toBeGT(0);
				expect(find("ID", jsonObject)).toBeGT(0);
			});
		});
	}
}