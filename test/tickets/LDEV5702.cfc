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

			it(title="setting cookies from many parallel threads does not break the response headers", body = function( currentSpec ) {
				var many = [];
				arraySet( many, 1, 200, "" );
				// the threads add and replace Set-Cookie headers in the same response at the same time
				loop times=20 {
					arrayEach( many, function( el, idx ){
						cookie[ "ldev5702_" & ( idx mod 5 ) ] = idx;
					}, true );
				}
				var headers = getPageContext().getHttpServletResponse().getHeaders( "Set-Cookie" );
				expect( isNull( headers ) ).toBeFalse();
				expect( headers.size() ).toBeGTE( 5 );
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