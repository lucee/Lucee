component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-2762", function() {
			it( title="Eval BIF function imageFormats()", body=function( currentSpec ) {
				var i = 0;
				var result = "";
				try {
					evaluate( "imageFormats();" ); 
				} catch (e) {
					result = e.message;
				}
				expect( result ).toBe( "" );
			});

			it( title="Checking calling BIF functions (with no args) via eval", body=function( currentSpec ) {
				var i = 0;
				var result = "";
				for ( var f in getFunctionList() ){
					var d = getFunctionData( f );
					if ( d.argmin neq 0 )
						continue; // only testing functions which can accept 0 arguments
					result = "";
					try {
						evaluate( "#f#();" ); 
					} catch (e) {
						systemOutput( d, true);
						result = e.message;
					}
					expect( result ).toBe("", 'evaluate( "#f#();" ) ');
					i++;
				}
			});
		});
	}

}