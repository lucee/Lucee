component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("testcase for StructEvery()", function() {
			variables.myStruct = {a:1,b=2,c=3,d=4,e=5};
			it(title="checking StructEvery() function", body=function( currentSpec ) {
				assertEquals( false, StructEvery( myStruct, function(key,value) { return value > 2;} )); 
			});
			it(title="checking Struct.Every() member function", body=function( currentSpec ) {
				assertEquals( true, myStruct.Every( function(key,value) { return isNumeric(value) } )); 
			});
		});
	}
}

