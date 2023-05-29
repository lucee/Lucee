component extends="org.lucee.cfml.test.LuceeTestCase" labels="syntax" {
	function run( testResults , testBox ) {
		describe( "Elvis operator on array", function() {
			it(title="simple array with Elvis operator", body=function() {
				try{
					array =[];
					result = array[1] ?: "true";
				} catch ( any e ){
					result = e.message;
				}
				expect(result).toBe("true");
			});

			it(title="simple array, returned from a function, used with Elvis operator", body=function() {
				try{
					tmpArr = GetArray();
					result = tmpArr[1] ?: "true";
				} catch ( any e ){
					result = e.message;
				}
				expect(result).toBe("true");
			});

			it(title="simple array, returned from a function call directly, used with Elvis operator", skip=true, body=function() {
				try{
					result = GetArray()[1] ?: "true";
				} catch ( any e ){
					result = e.message;
				}
				expect(result).toBe("true");
			});
		});

		describe( "Elvis operator on structure", function() {
			it(title="simple structure with Elvis operator", body=function() {
				try{
					struct =[];
					result = struct.one ?: "true";
				} catch ( any e ){
					result = e.message;
				}
				expect(result).toBe("true");
			});

			it(title="simple structure, returned from a function, used with Elvis operator", body=function() {
				try{
					tmpStruct = GetStruct();
					result = tmpStruct.one ?: "true";
				} catch ( any e ){
					result = e.message;
				}
				expect(result).toBe("true");
			});

			it(title="simple structure, returned from a function call directly, used with Elvis operator", skip=true, body=function() {
				try{
					result = GetStruct().one ?: "true";
				} catch ( any e ){
					result = e.message;
				}
				expect(result).toBe("true");
			});
		});

		describe( "Elvis operator on query", function() {
			it(title="simple query with Elvis operator", body=function() {
				try{
					query = queryNew("");
					result = query.name ?: "true";
				} catch ( any e ){
					result = e.message;
				}
				expect(result).toBe("true");
			});

			it(title="simple query, returned from a function, used with Elvis operator", body=function() {
				try{
					tmpQuery = GetQuery();
					result = tmpQuery.name ?: "true";
				} catch ( any e ){
					result = e.message;
				}
				expect(result).toBe("true");
			});

			it(title="simple query, returned from a function call directly, used with Elvis operator", skip=true, body=function() {
				try{
					result = GetQuery().name ?: "true";
				} catch ( any e ){
					result = e.message;
				}
				expect(result).toBe("true");
			});
		});
	}

	// private functions
	private function GetArray(){
		emptyArray = [];
		return emptyArray;
	}

	private function GetStruct(){
		emptyStruct = {};
		return emptyStruct;
	}

	private function GetQuery(){
		emptyQuery = queryNew("");
		return emptyquery;
	}
}