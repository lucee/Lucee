component extends = "org.lucee.cfml.test.LuceeTestCase" {
	
	function run ( testResults , testbox ){
		describe( "Testcase for LDEV-3135", function(){
			it(title = "arrayPop function with single parameters", body = function( currentSpec ){
				var arr = ["one","two","three","four","five"];
				var res = arrayPop(arr);
				expect(res).toBe("five");
			});

			it(title = "arrayPop member function with single parameters", body = function( currentSpec ){
				var arr = ["one","two","three","four","five"];
				var res = arr.pop();
				expect(res).toBe("five");
			});

			it(title = "arrayPop function empty array with defaultvalue", body = function( currentSpec ){
				var arr = [];
				var res = arraypop(arr, "one");
				expect(trim(res)).toBe("one");
				try {
					res = arraypop(arr);
				}
				catch(any e) {
					var error = e.message;
				}
				expect(trim(error)).toBe("can not pop Element from array, array is empty");
			});
		});
	}
}