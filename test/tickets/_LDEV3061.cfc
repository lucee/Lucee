component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function switchDefault(required numeric input){
        switch(arguments.input){
            case 1:
                return "item 1";
            case 2:
                return "item 2";
            default:
                return "its default";
        }
    }

    function switchCase(required numeric input){
        switch(arguments.input){
            case 1:
                return "case 1";
            case 2:
            default:
                return "its default inside case 2";
            case 3:
                return "case 3";
        }
    }

	function run ( testResults, testBox ){
		describe( "This testcase suit for LDEV-3061", function(){

			it( title="checking switch with normal default ", body = function ( currentSpec ) {
				expect(switchDefault(1)).toBe("item 1");
				expect(switchDefault(2)).toBe("item 2");
				expect(switchDefault(-1)).toBe("its default");
			});

			it( title="checing switch with seperate default ", body = function ( currentSpec ) {
				expect(switchCase(1)).toBe("case 1");
				expect(switchCase(-1)).toBe("its default inside case 2");
				expect(switchCase(3)).toBe("case 3");
				expect(switchCase(2)).toBe("its default inside case 2");
			});
		});
	}
}