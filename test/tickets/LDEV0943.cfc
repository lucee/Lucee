component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run(){
		describe( title="Test cases for LDEV-943", body=function(){
			it(title="Reading empty cookie", body=function(){
				cookie.myvar1 = "";
				expect(isNull(cookie.myvar1)).toBeFalse();
			});

			it(title="Reading an undefined variable", body=function(){
				// cookie.myvar = "";
				expect(isNull(cookie.myvar2)).toBeTrue();
			});

			it(title="Reading an null variable", body=function(){
				cookie.myvar3 = javaCast( "null", 0 );
				expect(isNull(cookie.myvar3)).toBeTrue();
			});
		});
	}
}