component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "list to array function with numeric value", function() {
			it("simple list to array function", function( currentSpec ){
				list = "6,7,8";
				array = listToarray(list);
				expect(isnumeric(array[1])).toBeTrue();
			});

			it("string to array by list to array function", function( currentSpec ){
				list = "678";
				array = listToarray(list, "");
				expect(isnumeric(array[1])).toBeTrue();
			});

			it("list to array function in for loop", function( currentSpec ){
				for (c in "1,2,3"){ 
					error = "";
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
				for (c in listToArray(123,"")){
					error = "";
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