component extends="org.lucee.cfml.test.LuceeTestCase" {
    function test() {
        return "abc";
    }
	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4892 - Invalid struct shorthand syntax", function() {
			it( title="check syntax colon with space", body=function( currentSpec ) {
				var x={ "#test()#" : function(){} };
			});
            it( title="check syntax colon no space", body=function( currentSpec ) {
				var x={ "#test()#":function(){} };
			});
            it( title="check syntax colon no space", body=function( currentSpec ) {
				var x={ "#test()#" = function(){} };
			});
            it( title="check syntax colon no space", body=function( currentSpec ) {
				var x={ "#test()#"=function(){} };
			});
		}); 
	}
}