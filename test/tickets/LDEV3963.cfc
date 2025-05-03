component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run ( testResults , testBox ) {
		describe("Testcase for LDEV-3963", function() {
			it( title="Using Colon (:) in tag syntax function attribute", body=function( currentSpec ) {
				var meta = getMetaData( testFunc );
				debug(meta);
				expect(meta).toHaveKey("secured:api");
				expect(meta["secured:api"]).toBe("");
			});

			it( title="Using Colon (:) in tag syntax function attribute with value", body=function( currentSpec ) {
				var meta = getMetaData( testFuncValue );
				debug(meta);
				expect(meta).toHaveKey("secured:api");
				expect(meta["secured:api"]).toBe("lucee");
			});

			xit( title="Using Colon (:) in script syntax function attribute", body=function( currentSpec ) {
				try {
					var result =_InternalRequest(
						template : "#createURI("LDEV3963")#\LDEV3963.cfm"
					).filecontent;
				}
				catch(any e) {
					var result = e.stacktrace;
				}
				expect(result).tobe("true");
			});
		});

		describe("Testcase for LDEV-3963 using remote:", function() {
			xit( title="Using unquoted [access:remote] in function definition", body=function( currentSpec ) {
				try {
					var res = new LDEV3963.test3963colonUnquoted();
					var meta = getMetaData( res ).functions[ 1 ];
					result = structKeyExists(meta,"ACCESS");
					expect( meta.access ).toBe( "remote" );
				}
				catch(any e) {
					var result = e.stacktrace;
				}
				expect(result).toBe("true");
			});

			it( title="Using quoted [access:'remote'] in function definition", body=function( currentSpec ) {
				try {
					var res = new LDEV3963.test3963colonQuoted();
					var meta = getMetaData( res ).functions[ 1 ];
					var result = structKeyExists(meta,"ACCESS");
					expect( meta.access ).toBe( "remote" );
				}
				catch(any e) {
					var result = e.stacktrace;
				}
				expect(result).toBe("true");
			});
		});

		describe("Testcase for LDEV-3963 using remote=", function() {
			xit( title="Using unquoted [access=remote] function definition", body=function( currentSpec ) {
				try {
					var result = new LDEV3963.test3963unquoted();
					var meta = getMetaData( res ).functions[ 1 ];
					result = structKeyExists(meta,"ACCESS");
					expect( meta.access ).toBe( "remote" );
				}
				catch(any e) {
					var result = e.stacktrace;
				}
				expect(result).toBe("true");
			});

			it( title="Using quoted [access='remote'] function definition", body=function( currentSpec ) {
				try {
					var res = new LDEV3963.test3963quoted();
					var meta = getMetaData( res ).functions[ 1 ];
					var result = structKeyExists(meta,"ACCESS");
					expect( meta.access ).toBe( "remote" );
				}
				catch(any e) {
					var result = e.stacktrace;
				}
				expect(result).toBe("true");
			});

		});
	}

	private string function createURI(string calledName, boolean contract=true){
		var base = getDirectoryFromPath( getCurrentTemplatePath() );
		var baseURI = contract ? contractPath( base ) : "/test/#listLast(base,"\/")#";
		return baseURI & "/" & calledName;
	}

	```
	<cffunction name="testFunc" access="private" secured:api>

	</cffunction>

	<cffunction name="testFuncValue" access="private" secured:api="lucee">

	</cffunction>

	```
}