component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("testcase for StructSome()", function() {
			variables.myStruct = {a:1,b=2,c=3,d=4,e=5};
			it(title="checking StructSome() function", body=function( currentSpec ) {
				assertEquals( true, StructSome( myStruct, function(key,value) { return value == 2; })); 
			});
			it(title="checking Struct.Some() member function", body=function( currentSpec ) {
				assertEquals( false, myStruct.Some( function(key,value) { return value > 5; })); 
			});
		});
	}
}

