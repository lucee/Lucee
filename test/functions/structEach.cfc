component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("testcase for StructEach()", function() {
			variables.animals = {"cow": "moo","pig": "oink"};
			it(title="checking StructEach() function", body=function( currentSpec ) {
				var animalList = "";
				StructEach(animals, function(key,value) { 
					animalList &= key& ":" & value & ", ";
				});
				assertEquals('cow:moo, pig:oink,',trim(animalList));
			});
			it(title="checking Struct.Each() member function", body=function( currentSpec ) {
				var animalList = "";
				animals.Each(function(key,value) { 
					animalList &= key& ":" & value & ", ";
				});
				assertEquals("cow:moo, pig:oink,",trim(animalList));
			});
		});
	}
}