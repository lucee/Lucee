component extends="org.lucee.cfml.test.LuceeTestCase" labels="http" {

	function beforeAll() {
		variables.httpbin = server.getTestService("httpbin");
		variables.hasHttpbin = structCount(variables.httpbin) > 0;
		variables.httpbinUrl = variables.hasHttpbin ? "http://#variables.httpbin.server#:#variables.httpbin.port#" : "";
		variables.dir = getTempDirectory() & "LDEV3452/"
		if(directoryExists(variables.dir)) directoryDelete(variables.dir,true);
		directoryCreate(variables.dir);
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-3452", function() {
			it( title="Checking cfhttp with path & file attributes", body=function( currentSpec ) {
				if ( !variables.hasHttpbin ) return;
				cfhttp( url="#variables.httpbinUrl#/anything" file="test.txt" method="GET" path="#variables.dir#" );
				result = fileExists("#variables.dir#/test.txt");
			
				expect(result).tobe("true");
			});

			it( title="Checking cfhttp with attribute path=path/filename", body=function( currentSpec ) {
				if ( !variables.hasHttpbin ) return;
				cfhttp( url="#variables.httpbinUrl#/anything" method="GET" path="#variables.dir#/file.txt" );
				result = fileExists("#variables.dir#/file.txt");
			
				expect(result).tobe("true");
			});

			it( title="Checking cfhttp with attribute path=path", body=function( currentSpec ) {
				try {
					cfhttp( url="https://raw.githubusercontent.com/lucee/Lucee/6.0/test/functions/images/lucee.png" method="GET" path="#variables.dir#" );
					result = fileExists("#variables.dir#/lucee.png");
				}
				catch(any e) {
					result = e.message;
				}
				expect(result).tobe("true");
			});

		}); 
	}

	function afterAll() { 
		directorydelete(variables.dir,true);
	}
}
