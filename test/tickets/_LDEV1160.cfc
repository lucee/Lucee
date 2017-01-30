component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.empDetails = queryNew("name,age,sex","varchar,integer,varchar", [['saravana',35,'male'],['Bob',20, 'female'],['pothy',25, 'male']]);
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1160", function() {
			it("checking getColumnList function, Query.getColumnList()", function( currentSpec ){
				try{
					var result = empDetails.getColumnList();
				} catch ( ANY e){
					var result = e.message;
				}
				expect(result).toBe("NAME,AGE,SEX");
			});


			it("checking getColumnList function with boolean value, Query.getColumnList(true)", function( currentSpec ){
				
				try{
					var result = empDetails.getColumnList(true);
				} catch ( ANY e){
					var result = e.message;
				}
				expect(result).toBe("NAME,AGE,SEX");
			});

			it("checking getColumnList function with boolean value, Query.getColumnList(false)", function( currentSpec ){
				try{
					var result = empDetails.getColumnList(false);
				} catch ( ANY e){
					var result = e.message;
				}
				expect(result).toBe("name,age,sex");
			});
		});
	}
}