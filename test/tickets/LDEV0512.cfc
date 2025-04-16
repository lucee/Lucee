component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "list to array function with numeric value", function() {
			it("simple list to array function", function( currentSpec ){
				var list = "6,7,8";
				var array = listToarray(list);
				expect(isnumeric(array[1])).toBeTrue();
			});

			it("string to array by list to array function", function( currentSpec ){
				var list = "678";
				var array = listToarray(list, "");
				expect(isnumeric(array[1])).toBeTrue();
			});

			it("list to array function in for loop", function( currentSpec ){
				for (var c in "1,2,3"){ 
					var error = "";
					try{
						c = c + 1;
					}
					catch (any e){
						error = e.message;
					}
					expect(error).toBe("");
				}
			});

			it("list to array function in for loop", function( currentSpec ){
				for (var c in listToArray(123,"")){
					var error = "";
					try{
						c = c + 1;
					}
					catch (any e){
						error = e.message;
					}
					expect(error).toBe("");
				}
			});
		});
	}
}