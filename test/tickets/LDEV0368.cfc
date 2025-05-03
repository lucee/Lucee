component extends="org.lucee.cfml.test.LuceeTestCase" labels="syntax" {
	function run( testResults , testBox ) {
		describe( "Elvis operator on array", function() {
			it(title="simple array with Elvis operator", body=function() {
				try{
					var array =[];
					var result = array[1] ?: "true";
				} catch ( any e ){
					result = e.message;
				}
				expect(result).toBe("true");
			});

			it(title="simple array, returned from a function, used with Elvis operator", body=function() {
				try{
					var tmpArr = GetArray();
					var result = tmpArr[1] ?: "true";
				} catch ( any e ){
					result = e.message;
				}
				expect(result).toBe("true");
			});

			it(title="simple array, returned from a function call directly, used with Elvis operator", skip=true, body=function() {
				try{
					var result = GetArray()[1] ?: "true";
				} catch ( any e ){
					result = e.message;
				}
				expect(result).toBe("true");
			});
		});

		describe( "Elvis operator on structure", function() {
			it(title="simple structure with Elvis operator", body=function() {
				try{
					var struct =[];
					var result = struct.one ?: "true";
				} catch ( any e ){
					result = e.message;
				}
				expect(result).toBe("true");
			});

			it(title="simple structure, returned from a function, used with Elvis operator", body=function() {
				try{
					var tmpStruct = GetStruct();
					var result = tmpStruct.one ?: "true";
				} catch ( any e ){
					result = e.message;
				}
				expect(result).toBe("true");
			});

			it(title="simple structure, returned from a function call directly, used with Elvis operator", skip=true, body=function() {
				try{
					var result = GetStruct().one ?: "true";
				} catch ( any e ){
					result = e.message;
				}
				expect(result).toBe("true");
			});
		});

		describe( "Elvis operator on query", function() {
			it(title="simple query with Elvis operator", body=function() {
				try{
					var query = queryNew("");
					var result = query.name ?: "true";
				} catch ( any e ){
					result = e.message;
				}
				expect(result).toBe("true");
			});

			it(title="simple query, returned from a function, used with Elvis operator", body=function() {
				try{
					var tmpQuery = GetQuery();
					var result = tmpQuery.name ?: "true";
				} catch ( any e ){
					result = e.message;
				}
				expect(result).toBe("true");
			});

			it(title="simple query, returned from a function call directly, used with Elvis operator", skip=true, body=function() {
				try{
					var result = GetQuery().name ?: "true";
				} catch ( any e ){
					result = e.message;
				}
				expect(result).toBe("true");
			});
		});
	}

	// private functions
	private function GetArray(){
		var emptyArray = [];
		return emptyArray;
	}

	private function GetStruct(){
		var emptyStruct = {};
		return emptyStruct;
	}

	private function GetQuery(){
		var emptyQuery = queryNew("");
		return emptyquery;
	}
}