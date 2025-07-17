component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.arr = [];
		arraySet(variables.arr, 1, 20, "");
	}

	function run( testResults , testBox ) {
		describe( title="LDEV-5702 cookies scope", body=function() {

			it(title="check cookie after parallel thread", body = function( currentSpec ) {

				cookie.ldev5702 = "ldev5702-parallel";
				arrayEach( variables.arr, function(el, idx){
					cookie.thread = idx;
				}, true);

				expect(cookie.ldev5702 ? :"").toBe("ldev5702-parallel");
			});

			it(title="check cookie after non parallel thread", body = function( currentSpec ) {

				cookie.ldev5702 = "ldev5702";
				arrayEach( variables.arr, function(el, idx){
					cookie.thread = idx;
				});

				expect(cookie.ldev5702 ? :"").toBe("ldev5702");
			});

		});
	}
}