component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-3963", function() {
			it( title="Using Colon (:) in tag syntax function attribute", body=function( currentSpec ) {
				var res = new LDEV3963.testTagEmpty();
				var meta = getMetaData( res ).functions[ 1 ];
				expect(meta).toHaveKey("secured:api");
				expect(meta["secured:api"]).toBe("");
			});

			it( title="Using Colon (:) in tag syntax function attribute with value", body=function( currentSpec ) {
				var res = new LDEV3963.testTagValue();
				var meta = getMetaData( res ).functions[ 1 ];
				expect(meta).toHaveKey("secured:api");
				expect(meta["secured:api"]).toBe("lucee");
			});

			it( title="Using Colon (:) in script syntax function attribute", body=function( currentSpec ) {
				try {
					var result = _InternalRequest(
						template: "#createURI("LDEV3963")#/LDEV3963.cfm"
					).filecontent;
				}
				catch(any e) {
					var result = e.message;
				}
				expect( trim(result) ).toBe("true");
			});
		});

		describe("Testcase for LDEV-3963 using remote:", function() {
			it( title="Using unquoted [access:remote] in function definition", body=function( currentSpec ) {
				try {
					var res = new LDEV3963.test3963colonUnquoted();
					var meta = getMetaData( res ).functions[ 1 ];
					expect( meta.access ).toBe( "remote" );
					var result = true;
				}
				catch(any e) {
					var result = e.message;
				}
				expect(result).toBe(true);
			});

			it( title="Using quoted [access:'remote'] in function definition", body=function( currentSpec ) {
				var res = new LDEV3963.test3963colonQuoted();
				var meta = getMetaData( res ).functions[ 1 ];
				expect( meta.access ).toBe( "remote" );
			});
		});

		describe("Testcase for LDEV-3963 using remote=", function() {
			it( title="Using unquoted [access=remote] function definition", body=function( currentSpec ) {
				try {
					var res = new LDEV3963.test3963unquoted();
					var meta = getMetaData( res ).functions[ 1 ];
					expect( meta.access ).toBe( "remote" );
					var result = true;
				}
				catch(any e) {
					var result = e.message;
				}
				expect(result).toBe(true);
			});

			it( title="Using quoted [access='remote'] function definition", body=function( currentSpec ) {
				var res = new LDEV3963.test3963quoted();
				var meta = getMetaData( res ).functions[ 1 ];
				expect( meta.access ).toBe( "remote" );
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}
