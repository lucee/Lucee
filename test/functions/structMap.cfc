component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("testcase for StructMap()", function() {
			variables.myStruct = [a:1,b=2,c=3];
			it(title="checking StructMap() function", body=function( currentSpec ) {
				assertEquals([A:5,B:10,C:15],Structmap(myStruct,function(key,value) {return value * 5}));
			});
			it(title="checking Struct.Map() member function", body=function( currentSpec ) {
				assertEquals(serializeJSON([A:5,B:10,C:15]),serializeJSON(myStruct.map(function(key,value) {return value * 5;})));
			});
		});
	}
}

