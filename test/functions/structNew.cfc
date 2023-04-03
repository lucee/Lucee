component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("testcase for StructNew()", function() {
			variables.myStruct = [ a:1,b=2,c=3 ];
			it(title="function structNew(ordered-casesensitive)", body=function( currentSpec ) {
				
				var sct=structNew("ordered-casesensitive");
				sct["a"]="a";
				sct["b"]="b";
				sct["c"]="c";
				sct["A"]="A";
				sct["B"]="B";
				
				assertEquals( sct.keyExists("c"),true);
				assertEquals( sct.keyExists("C"),false);
				assertEquals( sct.keyList(),"a,b,c,A,B");
			});
		});
	}
}

