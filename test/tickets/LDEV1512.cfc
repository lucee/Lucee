component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for LDEV-1512", function() {
			it(title = "Checking JSON with createTimeSpan string, in arrayFormat", body = function( currentSpec ) {
				var arry= [createTimeSpan(0,0,1,0),123];
				var arryJSON = serializeJson(arry);
				var result = "";
				try {
					var deserializedValue = deserializeJson(arryJSON);
					result = deserializedValue[1];
				}
				catch( any e ) {
					result = e.message;
				}
				expect(result).toBe(arry[1]);
			});

			it(title = "Checking JSON with createTimeSpan string, in StructFormat", body = function( currentSpec ) {
				var Str = {"TimeSpan": createTimeSpan(0,0,1,0),"List": 123};
				var StrJSON = serializeJson(Str);
				var result = "";
				try {
					var deserializedValue = deserializeJson(StrJSON);
					result = deserializedValue.TimeSpan;
				}
				catch( any e ) {
					result = e.message;
				}
				expect(result).toBe(Str.TimeSpan);
			});

			it(title = "Checking JSON with createTimeSpan string, in QueryFormat", body = function( currentSpec ) {
				var Qry = QueryNew("TimeSpan,List", "String,string", [[createTimeSpan(0,0,1,0), 1234]]);
				var QryJSON = serializeJson(Qry);
				var result = "";
				try {
					var deserializedValue = deserializeJson(QryJSON);
					result = deserializedValue.TimeSpan;
				}
				catch( any e ) {
					result = e.message;
				}
				expect(result).toBe(Qry.TimeSpan);
			});
		});
	}
}