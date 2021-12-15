component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test case for LDEV-1829", function() {
			it(title = "Checking typed array with struct", body = function( currentSpec ) {
				var arr=arrayNew(type:"struct");
				arr.append({a:1});
				expect(arr[1]).toBeTypeOf('struct');
				var result = "";
				try {
					arr.append('test');
				} catch ( any e){
					result= e.message;
				}
				expect(result).NotToBe("");
			});

			it(title = "Checking typed array  with boolean", body = function( currentSpec ) {
				var arr=arrayNew(type:"boolean");
				arr.append(false);
				expect(arr[1]).toBe(false);
				var result = "";
				try {
					arr.append('test');
				} catch ( any e){
					result= e.message;
				}
				expect(result).NotToBe("");
			});

			it(title = "Checking typed array  with string", body = function( currentSpec ) {
				var arr = arrayNew(type:"string");
				arr.append("xxx");
				expect(arr[1]).toBe("xxx");
				var result = "";
				try {
					arr.append({test:1});
				} catch ( any e){
					result= e.message;
				}
				expect(result).NotToBe("");
			});
		});
	}
}
