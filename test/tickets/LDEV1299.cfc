component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq"{
	variables.isLucee5 = false;
	if( structKeyExists(server, "lucee") && listFirst(server.lucee.version, ".") == "5" )
		variables.isLucee5 = true;

	function isNotSupported(s1) {
		return s1;
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1299", body=function() {
			it(title="checking query with cachedAfter Attribute", skip=isNotSupported(!variables.isLucee5), body = function( currentSpec ) {
				var uri = createURI("LDEV1299")
				var result = _InternalRequest(
					template:"#uri#/test.cfm"
				);
				expect(result.filecontent.trim()).toBe('3');
			});

			it(title="checking query with cachedAfter Attribute", skip=isNotSupported(variables.isLucee5), body = function( currentSpec ) {
				var qry = queryNew("id,name,mail,showtime", "integer,varchar,varchar,timestamp", [
						[1, "pothy", "pothy@test.com", now()],
						[2, "mitrah", "mitrah@test.com", now()],
						[3, "soft", "soft@test.com", now()]
					]);
				var cacheAfterThis = dateAdd("s", 2, now());
				var qryToCache = queryExecute(
					"SELECT * FROM qry",
					[],
					{dbtype="query", qry=qry, cachedAfter=cacheAfterThis}
				);
				sleep(5000);
				var qryFrmCache = queryExecute(
					"SELECT * FROM qry",
					[],
					{dbtype="query", qry=qry, cachedAfter=cacheAfterThis}
				);
				assertEquals(qryFrmCache.RECORDCOUNT EQ 3, true);
			});
		});
	}
	// private Function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}