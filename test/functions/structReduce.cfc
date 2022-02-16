component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("testcase for StructReduce()", function() {
			variables.myStruct = {a:1,b=2,c=3};
			it(title="checking StructReduce() function", body=function( currentSpec ) {
				assertEquals(16,StructReduce(mystruct,function(result,key,value){return result+value},10));
			});
			it(title="checking Struct.Reduce() member function", body=function( currentSpec ) {
				assertEquals(16,mystruct.Reduce(function(result,key,value){return result+value},10));
			});
		});
	}
}