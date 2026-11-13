component extends="org.lucee.cfml.test.LuceeTestCase" labels="udf" {
	function getNameFunctionWasCalledWith() { return getFunctionCalledName(); }
	function run( testResults, testBox ) {
		describe( "LDEV-3596 getFunctionCalledName", function() {
			it( "direct call keeps the case it was called with", function() {
				expect( compare( getNameFunctionWasCalledWith(), "getNameFunctionWasCalledWith" ) ).toBe( 0 );
			});
			it( "this. call keeps the case", function() {
				expect( compare( this.getNameFunctionWasCalledWith(), "getNameFunctionWasCalledWith" ) ).toBe( 0 );
			});
			it( "variables. call keeps the case", function() {
				expect( compare( variables.getNameFunctionWasCalledWith(), "getNameFunctionWasCalledWith" ) ).toBe( 0 );
			});
			it( "alias keeps the alias case", function() {
				variables.myAlias = variables.getNameFunctionWasCalledWith;
				expect( compare( myAlias(), "myAlias" ) ).toBe( 0 );
			});
		});
	}
}
